package net.ultragrav.knet

import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.compression.Lz4FrameDecoder
import io.netty.handler.codec.compression.Lz4FrameEncoder
import net.ultragrav.knet.exception.DisconnectedException
import net.ultragrav.knet.packet.PacketHandler
import net.ultragrav.knet.packet.packets.PacketProxyCall
import net.ultragrav.knet.packet.encoding.PacketDecoder
import net.ultragrav.knet.packet.encoding.PacketEncoder
import net.ultragrav.knet.proxy.CallHandlerMap
import net.ultragrav.knet.proxy.ProxyCallProviderImpl
import net.ultragrav.knet.proxy.ProxyCaller
import net.ultragrav.knet.proxy.ProxyRegistrar
import java.net.SocketAddress

class KNetClient(val host: SocketAddress) : ProxyCaller, ProxyRegistrar {
    private val clientGroup by lazy { NioEventLoopGroup() }

    lateinit var channel: Channel

    override val callProvider = ProxyCallProviderImpl(this)
    private val proxies = CallHandlerMap()

    suspend fun connect() {
        val bootstrap = Bootstrap()
            .channel(NioSocketChannel::class.java)
            .group(clientGroup)
            .handler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    ch.pipeline().addLast(
                        Lz4FrameEncoder(),
                        Lz4FrameDecoder(),
                        PacketDecoder(),
                        PacketEncoder(),
                        PacketHandler(this@KNetClient),
                        callProvider.DisconnectHandler()
                    )
                }
            })
        channel = bootstrap.connect(host).awaitKt().channel()
    }

    override suspend fun sendCall(call: PacketProxyCall) {
        if (!channel.isActive) {
            throw DisconnectedException()
        }
        channel.writeAndFlush(call).awaitKt()
    }

    override suspend fun handleCall(call: PacketProxyCall): ByteArray {
        return proxies.call(call.className, call.functionName, call.args)
    }

    suspend fun shutdown() {
        channel.close().awaitKt()
        clientGroup.shutdownGracefully().awaitKt()
    }

    override fun <T> registerProxy(inter: Class<T>, proxy: ProxyCallHandler<T>) {
        proxies.registerProxy(inter, proxy)
    }
}