package net.ultragrav.knet.proxy

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import kotlinx.coroutines.*
import kotlinx.serialization.ExperimentalSerializationApi
import net.ultragrav.knet.KNet
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
        val continuation: Continuation<ByteArray>,
        val timestamp: Long = System.currentTimeMillis()
    )

    private val responseHandlers = mutableMapOf<Long, ResponseHandler>()

    override suspend fun callProxyFunction(
        interfaceName: String,
        functionName: String,
        args: Array<ByteArray>
    ): ByteArray {
        val exception = DisconnectedException()
        try {
            return withContext(Dispatchers.IO) {
                val id = id.incrementAndGet()

                val call = PacketProxyCall(id, interfaceName, functionName, args)

                return@withContext coroutineScope { // Make sure that if sendCall
                    // fails then the continuation is also cancelled
                    launch { caller.sendCall(call) }
                    withTimeout(KNet.callTimeout) {
                        suspendCancellableCoroutine { cont ->
                            cont.invokeOnCancellation {
                                synchronized(responseHandlers) { responseHandlers.remove(id) }
                            }
                            synchronized(responseHandlers) {
                                responseHandlers[id] = ResponseHandler(interfaceName, functionName, cont)
                            }
                        }
                    }
                }

            }
        } catch (e: DisconnectedException) {
            throw exception
        }
    }

    internal fun handleResponse(response: PacketResponse) {
        val responseHandler = synchronized(responseHandlers) { responseHandlers.remove(response.id) } ?: return
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
        val resume = synchronized(responseHandlers) {
            val list = responseHandlers.values.toList()
            responseHandlers.clear()
            list
        }
        resume.forEach { it.continuation.resumeWithException(DisconnectedException()) }
    }

    internal inner class DisconnectHandler : ChannelInboundHandlerAdapter() {
        override fun channelInactive(ctx: ChannelHandlerContext) {
            runCatching { handleDisconnect() }.onFailure { it.printStackTrace() }
            ctx.fireChannelInactive()
        }
    }
}