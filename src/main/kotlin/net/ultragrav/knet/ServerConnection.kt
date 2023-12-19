package net.ultragrav.knet

import io.netty.channel.Channel
import net.ultragrav.knet.exception.DisconnectedException
import net.ultragrav.knet.packet.packets.PacketProxyCall
import net.ultragrav.knet.proxy.ProxyCallProviderImpl
import net.ultragrav.knet.proxy.ProxyCaller

class ServerConnection(val server: KNetServer, val channel: Channel) : ProxyCaller() {
    lateinit var parent: Any

    override suspend fun sendCall(call: PacketProxyCall) {
        if (!channel.isActive) {
            throw DisconnectedException()
        }
        channel.writeAndFlush(call).awaitKt()
    }

    override suspend fun handleCall(call: PacketProxyCall): ByteArray {
        return server.proxies.call(call.className, call.functionName, call.args)
    }
}