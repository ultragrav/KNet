package net.ultragrav.knet.packet.encoding

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder

class PacketDecoder : ByteToMessageDecoder() {
    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        if (buf.readableBytes() < 4) {
            return
        }

        buf.markReaderIndex()
        val length = buf.readInt()
        if (buf.readableBytes() < length) {
            buf.resetReaderIndex()
            return
        }

        val id = buf.readByte()
        val bytes = ByteArray(length - 1)
        buf.readBytes(bytes)

        out.add(PacketCodec.decode(id.toInt(), bytes))
    }
}