package net.ultragrav.knet.proxy

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import kotlinx.coroutines.Dispatchers
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

    private val responseHandlers = mutableMapOf<Long, Continuation<ByteArray>>()

    override suspend fun callProxyFunction(
        interfaceName: String,
        functionName: String,
        args: Array<ByteArray>
    ): ByteArray = withContext(Dispatchers.IO) {
        val id = id.incrementAndGet()

        val call = PacketProxyCall(id, interfaceName, functionName, args)
        caller.sendCall(call)

        return@withContext suspendCancellableCoroutine { cont ->
            responseHandlers[id] = cont
            cont.invokeOnCancellation {
                responseHandlers.remove(id)
            }
        }
    }

    internal fun handleResponse(response: PacketResponse) {
        val continuation = responseHandlers.remove(response.id) ?: return
        when (response.state) {
            PacketResponse.STATE_SUCCESS -> continuation.resume(response.returnValue)
            PacketResponse.STATE_EXCEPTION -> continuation.resumeWithException(response.exception!!)
        }
    }

    internal fun handleDisconnect() {
        responseHandlers.values.forEach { it.resumeWithException(DisconnectedException()) }
        responseHandlers.clear()
    }

    internal inner class DisconnectHandler : ChannelInboundHandlerAdapter() {
        override fun channelInactive(ctx: ChannelHandlerContext) {
            handleDisconnect()
            ctx.fireChannelInactive()
        }
    }
}