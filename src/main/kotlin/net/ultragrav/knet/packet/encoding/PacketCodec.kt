package net.ultragrav.knet.packet.encoding

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import net.ultragrav.knet.packet.packets.PacketProxyCall
import net.ultragrav.knet.packet.packets.PacketResponse
import net.ultragrav.kserializer.kotlinx.KJson

@OptIn(ExperimentalSerializationApi::class)
object PacketCodec {
    fun encode(packet: Any): Pair<Int, ByteArray> {
        return when (packet) {
            is PacketProxyCall -> 0 to KJson.encodeToByteArray<PacketProxyCall>(packet)
            is PacketResponse -> 1 to KJson.encodeToByteArray<PacketResponse>(packet)
            else -> throw IllegalArgumentException("Unknown packet type: ${packet::class.simpleName}")
        }

    }

    fun decode(id: Int, bytes: ByteArray): Any {
        return when (id) {
            0 -> KJson.decodeFromByteArray<PacketProxyCall>(bytes)
            1 -> KJson.decodeFromByteArray<PacketResponse>(bytes)
            else -> throw IllegalArgumentException("Unknown packet id: $id")
        }
    }
}