package net.ultragrav.knet

import io.netty.channel.Channel
import net.ultragrav.knet.packet.packets.PacketProxyCall
import net.ultragrav.knet.proxy.ProxyCaller

class ServerConnection(val server: KNetServer, val channel: Channel) : ProxyCaller() {

    lateinit var attachment: Any

    override suspend fun sendCall(call: PacketProxyCall) {
        channel.writeAndFlush(call).awaitKt()
    }

    override suspend fun handleCall(call: PacketProxyCall): ByteArray {
        return server.proxies.call(call.className, call.functionName, call.args)
    }
}