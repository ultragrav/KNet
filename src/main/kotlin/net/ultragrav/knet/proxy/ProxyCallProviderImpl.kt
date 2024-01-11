package net.ultragrav.knet.proxy

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import net.ultragrav.knet.ProxyCallProvider
import net.ultragrav.knet.exception.DisconnectedException
import net.ultragrav.knet.packet.packets.PacketProxyCall
import net.ultragrav.knet.packet.packets.PacketResponse
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@OptIn(ExperimentalSerializationApi::class)
class ProxyCallProviderImpl(private val caller: ProxyCaller) : ProxyCallProvider {
    private val id = AtomicLong(0)

    private class ResponseHandler(
        val interfaceName: String,
        val functionName: String,
        val continuation: Continuation<ByteArray>
    )

    private val responseHandlers = mutableMapOf<Long, ResponseHandler>()

    override suspend fun callProxyFunction(
        interfaceName: String,
        functionName: String,
        args: Array<ByteArray>
    ): ByteArray = withContext(Dispatchers.IO) {
        val id = id.incrementAndGet()

        val call = PacketProxyCall(id, interfaceName, functionName, args)
        launch { caller.sendCall(call) }

        return@withContext suspendCancellableCoroutine { cont ->
            responseHandlers[id] = ResponseHandler(interfaceName, functionName, cont)
            cont.invokeOnCancellation {
                responseHandlers.remove(id)
            }
        }
    }

    internal fun handleResponse(response: PacketResponse) {
        val responseHandler = responseHandlers.remove(response.id) ?: return
        when (response.state) {
            PacketResponse.STATE_SUCCESS -> responseHandler.continuation.resume(response.returnValue)
            PacketResponse.STATE_EXCEPTION -> {
                // Inject line into stack trace
                val exception = response.exception!!
                val stackTrace = exception.stackTrace
                val newStackTrace = arrayOfNulls<StackTraceElement>(stackTrace.size + 1)

                newStackTrace[0] = StackTraceElement(
                    "!! Remote[$caller]@${responseHandler.interfaceName}",
                    responseHandler.functionName,
                    "?",
                    -1
                )
                System.arraycopy(stackTrace, 0, newStackTrace, 1, stackTrace.size)

                exception.stackTrace = newStackTrace

                responseHandler.continuation.resumeWithException(exception)
            }
        }
    }

    internal fun handleDisconnect() {
        responseHandlers.values.forEach { it.continuation.resumeWithException(DisconnectedException()) }
        responseHandlers.clear()
    }

    internal inner class DisconnectHandler : ChannelInboundHandlerAdapter() {
        override fun channelInactive(ctx: ChannelHandlerContext) {
            handleDisconnect()
            ctx.fireChannelInactive()
        }
    }
}