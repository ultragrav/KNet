package net.ultragrav.knet.compression

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import net.jpountz.lz4.LZ4Factory

class KNetLZ4FrameDecoder : ByteToMessageDecoder() {
    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        buf.markReaderIndex()
        if (buf.readableBytes() < 4) return
        val frameSize = buf.readInt()
        if (buf.readableBytes() < frameSize) {
            buf.resetReaderIndex()
            return
        }
        val length = buf.readInt()
        val array = ByteArray(frameSize - 4)
        buf.readBytes(array)
        val decompressed = LZ4Factory.fastestJavaInstance().fastDecompressor()
            .decompress(array, length)
        val buffer = ctx.alloc().buffer(decompressed.size)
        buffer.writeBytes(decompressed)
        out.add(buffer)
    }
}