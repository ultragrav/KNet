package net.ultragrav.knet

import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import net.ultragrav.knet.packet.PacketProxyCall
import net.ultragrav.knet.packet.encoding.PacketDecoder
import net.ultragrav.knet.packet.encoding.PacketEncoder
import net.ultragrav.knet.proxy.CallHandlerMap
import net.ultragrav.knet.proxy.ProxyCallProviderImpl
import net.ultragrav.knet.proxy.ProxyCaller
import java.net.SocketAddress

class KNetClient(val host: SocketAddress) : ProxyCaller {
    private lateinit var channel: Channel

    override val callProvider = ProxyCallProviderImpl(this)
    val proxies = CallHandlerMap()

    fun connect() {
        val clientGroup = NioEventLoopGroup()
        channel = Bootstrap()
            .channel(NioSocketChannel::class.java)
            .group(clientGroup)
            .handler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    ch.pipeline().addLast(PacketDecoder(), PacketEncoder(), NetHandler(this@KNetClient))
                }
            })
            .connect(host)
            .sync()
            .channel()
    }

    override fun sendCall(call: PacketProxyCall) {
        channel.writeAndFlush(call)
    }

    override suspend fun handleCall(call: PacketProxyCall): ByteArray {
        return proxies.call(call.className, call.functionName, call.args)
    }

    fun disconnect() {
        channel.close().sync()
    }
}