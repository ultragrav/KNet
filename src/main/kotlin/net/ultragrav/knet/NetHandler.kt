package net.ultragrav.knet

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import kotlinx.coroutines.*
import net.ultragrav.knet.packet.PacketProxyCall
import net.ultragrav.knet.packet.PacketResponse
import net.ultragrav.knet.proxy.ProxyCaller
import kotlin.coroutines.suspendCoroutine

class NetHandler(val caller: ProxyCaller) : SimpleChannelInboundHandler<Any>() {
    override fun channelRead0(ctx: ChannelHandlerContext, msg: Any) {
        when (msg) {
            is PacketProxyCall -> {
                CoroutineScope(KNet.defaultDispatcher).launch {
                    try {
                        val bytes = caller.handleCall(msg)
                        withContext(Dispatchers.IO) { ctx.channel().writeAndFlush(PacketResponse(msg.id, bytes)) }
                    } catch (e: Throwable) {
                        withContext(Dispatchers.IO) { ctx.channel().writeAndFlush(PacketResponse(msg.id, e)) }
                    }
                }
            }
            is PacketResponse -> caller.callProvider.handleResponse(msg)
        }
    }
}