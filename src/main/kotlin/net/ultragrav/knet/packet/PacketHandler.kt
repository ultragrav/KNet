package net.ultragrav.knet.packet

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.ultragrav.knet.KNet
import net.ultragrav.knet.packet.packets.PacketProxyCall
import net.ultragrav.knet.packet.packets.PacketResponse
import net.ultragrav.knet.proxy.KNetCallerContextElement
import net.ultragrav.knet.proxy.ProxyCaller

internal class PacketHandler(private val caller: ProxyCaller) : SimpleChannelInboundHandler<Any>() {
    override fun channelRead0(ctx: ChannelHandlerContext, msg: Any) {
        when (msg) {
            is PacketProxyCall -> {
                CoroutineScope(KNet.defaultDispatcher + KNetCallerContextElement(caller)).launch {
                    try {
                        val bytes = caller.handleCall(msg)
                        ctx.channel().writeAndFlush(PacketResponse(msg.id, bytes))
                    } catch (e: Throwable) {
                        ctx.channel().writeAndFlush(PacketResponse(msg.id, e))
                    }
                }
            }

            is PacketResponse -> caller.callProvider.handleResponse(msg)
        }
    }
}