package net.ultragrav.knet.serialization

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import net.ultragrav.kserializer.kotlinx.KJson
import kotlin.reflect.KType

object KJsonSerializer : Serializer {

    override val module: SerializersModule
        get() = KJson.defaultModule

    override fun <T> serialize(kxStrategy: SerializationStrategy<T>?, type: KType, obj: T): ByteArray {
        return KJson.encodeToByteArray(kxStrategy!!, obj)
    }

    override fun <T> deserialize(kxStrategy: DeserializationStrategy<T>?, type: KType, bytes: ByteArray): T {
        return KJson.decodeFromByteArray(kxStrategy!!, bytes)
    }
}