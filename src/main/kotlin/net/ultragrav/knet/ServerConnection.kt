package net.ultragrav.knet

import io.netty.channel.Channel
import net.ultragrav.knet.packet.PacketProxyCall
import net.ultragrav.knet.proxy.ProxyCallProviderImpl
import net.ultragrav.knet.proxy.ProxyCaller

class ServerConnection(val server: KNetServer, val channel: Channel) : ProxyCaller {
    lateinit var parent: Any

    override val callProvider = ProxyCallProviderImpl(this)

    override fun sendCall(call: PacketProxyCall) {
        channel.writeAndFlush(call)
    }

    override suspend fun handleCall(call: PacketProxyCall): ByteArray {
        return server.proxies.call(call.className, call.functionName, call.args)
    }
}