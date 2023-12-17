package net.ultragrav.knet.packet.encoding

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder

class PacketEncoder : MessageToByteEncoder<Any>() {
    override fun encode(ctx: ChannelHandlerContext, msg: Any, out: ByteBuf) {
        val (id, bytes) = PacketCodec.encode(msg)
        out.writeInt(bytes.size + 1)
        out.writeByte(id)
        out.writeBytes(bytes)
    }
}