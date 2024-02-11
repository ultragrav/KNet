package net.ultragrav.knet

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import net.ultragrav.knet.compression.KNetLZ4FrameEncoder
import net.ultragrav.knet.compression.KNetLZ4FrameDecoder
import net.ultragrav.knet.packet.PacketHandler
import net.ultragrav.knet.packet.encoding.PacketDecoder
import net.ultragrav.knet.packet.encoding.PacketEncoder
import net.ultragrav.knet.proxy.CallHandlerMap
import net.ultragrav.knet.proxy.ProxyRegistrar

class KNetServer(val port: Int) : ProxyRegistrar {
    private val bossGroup by lazy { NioEventLoopGroup() }
    private val workerGroup by lazy { NioEventLoopGroup() }

    private var active = true

    lateinit var channel: Channel

    internal val proxies = CallHandlerMap()

    val connected = mutableSetOf<ServerConnection>()

    var listener: KNetServerListener? = null

    suspend fun run() {
        try {
            channel = ServerBootstrap()
                .channel(NioServerSocketChannel::class.java)
                .group(bossGroup, workerGroup)
                .childHandler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel) {
                        if (!active) {
                            ch.close()
                            return
                        }

                        val serverConnection = ServerConnection(this@KNetServer, ch)
                        ch.pipeline().addLast(
                            KNetLZ4FrameEncoder(),
                            KNetLZ4FrameDecoder(),
                            PacketDecoder(),
                            PacketEncoder(),
                            PacketHandler(serverConnection),
                            DisconnectHandler(serverConnection),
                            serverConnection.callProvider.DisconnectHandler()
                        )

                        synchronized(connected) {
                            connected.add(serverConnection)
                        }

                        runCatching { listener?.onConnect(serverConnection) }
                            .onFailure { it.printStackTrace() }
                    }
                })
                .bind(port)
                .awaitKt()
                .channel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun stop() {
        active = false
        val copy = synchronized(connected) { connected.toList() }
        copy.forEach { it.channel.close().awaitKt() }
        channel.close().awaitKt()
        bossGroup.shutdownGracefully().awaitKt()
        workerGroup.shutdownGracefully().awaitKt()
        synchronized(connected) { connected.clear() }
    }

    inner class DisconnectHandler(private val serverConnection: ServerConnection) : ChannelInboundHandlerAdapter() {
        override fun channelInactive(ctx: ChannelHandlerContext) {
            runCatching { listener?.onDisconnect(serverConnection) }
                .onFailure { it.printStackTrace() }

            synchronized(connected) {
                connected.remove(serverConnection)
            }
            ctx.fireChannelInactive()
        }
    }

    override fun <T> registerProxy(inter: Class<T>, proxy: ProxyCallHandler<T>) {
        proxies.registerProxy(inter, proxy)
    }
}