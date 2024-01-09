package net.ultragrav.knet.compression

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import io.netty.handler.codec.MessageToByteEncoder
import net.jpountz.lz4.LZ4Factory

class LZ4CompressHandler : MessageToByteEncoder<ByteBuf>() {
    override fun encode(ctx: ChannelHandlerContext, msg: ByteBuf, out: ByteBuf) {
        val array = ByteArray(msg.readableBytes())
        msg.readBytes(array)
        val compressed = LZ4Factory.fastestInstance().fastCompressor()
            .compress(array)
        out.writeInt(array.size)
        out.writeBytes(compressed)
    }
}