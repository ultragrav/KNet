package net.ultragrav.knet.compression

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import net.jpountz.lz4.LZ4Factory

class LZ4DecompressHandler : ByteToMessageDecoder() {
    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        val length = buf.readInt()
        val array = ByteArray(buf.readableBytes())
        buf.readBytes(array)
        val decompressed = LZ4Factory.fastestInstance().fastDecompressor()
            .decompress(array, length)
        out.add(decompressed)
    }
}