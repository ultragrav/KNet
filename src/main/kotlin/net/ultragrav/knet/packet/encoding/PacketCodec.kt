package net.ultragrav.knet.packet.encoding

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import net.ultragrav.knet.packet.PacketProxyCall
import net.ultragrav.knet.packet.PacketResponse

@OptIn(ExperimentalSerializationApi::class)
object PacketCodec {
    fun encode(packet: Any): Pair<Int, ByteArray> {
        return when (packet) {
            is PacketProxyCall -> 0 to ProtoBuf.encodeToByteArray<PacketProxyCall>(packet)
            is PacketResponse -> 1 to ProtoBuf.encodeToByteArray<PacketResponse>(packet)
            else -> throw IllegalArgumentException("Unknown packet type: ${packet::class.simpleName}")
        }

    }

    fun decode(id: Int, bytes: ByteArray): Any {
        return when (id) {
            0 -> ProtoBuf.decodeFromByteArray<PacketProxyCall>(bytes)
            1 -> ProtoBuf.decodeFromByteArray<PacketResponse>(bytes)
            else -> throw IllegalArgumentException("Unknown packet id: $id")
        }
    }
}