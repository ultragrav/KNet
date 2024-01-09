package net.ultragrav.knet.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.serializer
import net.ultragrav.kserializer.kotlinx.KJson
import kotlin.reflect.KType

@OptIn(ExperimentalSerializationApi::class)
object KJsonSerializer : Serializer {
    override fun <T> serialize(type: KType, obj: T): ByteArray {
        return KJson.encodeToByteArray(serializer(type), obj)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> deserialize(type: KType, bytes: ByteArray): T {
        return KJson.decodeFromByteArray(serializer(type), bytes) as T
    }
}