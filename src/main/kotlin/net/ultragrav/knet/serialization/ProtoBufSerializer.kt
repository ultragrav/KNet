package net.ultragrav.knet.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.serializer
import kotlin.reflect.KType

@OptIn(ExperimentalSerializationApi::class)
object ProtoBufSerializer : Serializer {
    override fun <T> serialize(type: KType, obj: T): ByteArray {
        return ProtoBuf.encodeToByteArray(serializer(type), obj)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> deserialize(type: KType, bytes: ByteArray): T {
        return ProtoBuf.decodeFromByteArray(serializer(type), bytes) as T
    }
}