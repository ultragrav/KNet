package net.ultragrav.knet

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.compression.Lz4FrameDecoder
import io.netty.handler.codec.compression.Lz4FrameEncoder
import net.ultragrav.knet.packet.PacketHandler
import net.ultragrav.knet.packet.encoding.PacketDecoder
import net.ultragrav.knet.packet.encoding.PacketEncoder
import net.ultragrav.knet.proxy.CallHandlerMap
import net.ultragrav.knet.proxy.ProxyRegistrar

class KNetServer(val port: Int) : ProxyRegistrar {
    private val bossGroup by lazy { NioEventLoopGroup() }
    private val workerGroup by lazy { NioEventLoopGroup() }

    lateinit var channel: Channel

    internal val proxies = CallHandlerMap()

    val connected = mutableSetOf<ServerConnection>()

    fun run() {
        try {
            channel = ServerBootstrap()
                .channel(NioServerSocketChannel::class.java)
                .group(bossGroup, workerGroup)
                .childHandler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel) {
                        val serverConnection = ServerConnection(this@KNetServer, ch)
                        ch.pipeline().addLast(
                            Lz4FrameEncoder(),
                            Lz4FrameDecoder(),
                            PacketDecoder(),
                            PacketEncoder(),
                            PacketHandler(serverConnection),
                            DisconnectHandler(serverConnection),
                            serverConnection.callProvider.DisconnectHandler()
                        )
                        connected.add(serverConnection)
                    }
                })
                .bind(port)
                .sync()
                .channel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun stop() {
        channel.close().awaitKt()
        bossGroup.shutdownGracefully().awaitKt()
        workerGroup.shutdownGracefully().awaitKt()
    }

    inner class DisconnectHandler(private val serverConnection: ServerConnection) : ChannelInboundHandlerAdapter() {
        override fun channelInactive(ctx: ChannelHandlerContext) {
            connected.remove(serverConnection)
            ctx.fireChannelInactive()
        }
    }

    override fun <T> registerProxy(inter: Class<T>, proxy: ProxyCallHandler<T>) {
        proxies.registerProxy(inter, proxy)
    }
}