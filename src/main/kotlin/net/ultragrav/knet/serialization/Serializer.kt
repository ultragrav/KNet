package net.ultragrav.knet.serialization

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.serializer
import net.ultragrav.kserializer.kotlinx.KJson
import kotlin.reflect.KType
import kotlin.reflect.typeOf

interface Serializer {
    fun <T> serialize(kxStrategy: SerializationStrategy<T>?, type: KType, obj: T): ByteArray
    fun <T> deserialize(kxStrategy: DeserializationStrategy<T>?, type: KType, bytes: ByteArray): T
}

inline fun <reified T> Serializer.serialize(obj: T): ByteArray = serialize(
    try {
        KJson.defaultModule.serializer()
    } catch (e: Exception) {
        null
    },
    typeOf<T>(), obj
)
inline fun <reified T> Serializer.deserialize(bytes: ByteArray): T = deserialize(
    try {
        KJson.defaultModule.serializer()
    } catch (e: Exception) {
        null
    },
    typeOf<T>(), bytes
)