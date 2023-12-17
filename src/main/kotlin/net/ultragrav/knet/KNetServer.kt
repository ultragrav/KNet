package net.ultragrav.knet

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import net.ultragrav.knet.packet.encoding.PacketDecoder
import net.ultragrav.knet.packet.encoding.PacketEncoder
import net.ultragrav.knet.proxy.CallHandlerMap

class KNetServer(val port: Int) {
    private val bossGroup by lazy { NioEventLoopGroup() }
    private val workerGroup by lazy { NioEventLoopGroup() }

    private lateinit var channel: Channel

    val proxies = CallHandlerMap()

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
                            PacketDecoder(),
                            PacketEncoder(),
                            NetHandler(serverConnection),
                            DisconnectHandler(serverConnection)
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

    fun stop() {
        channel.close().sync()
        bossGroup.shutdownGracefully()
        workerGroup.shutdownGracefully()
    }

    inner class DisconnectHandler(private val serverConnection: ServerConnection) : ChannelInboundHandlerAdapter() {
        override fun channelInactive(ctx: ChannelHandlerContext) {
            connected.remove(serverConnection)
            ctx.fireChannelInactive()
        }
    }
}