package net.ultragrav.knet.serialization

import kotlin.reflect.KType
import kotlin.reflect.typeOf

interface Serializer {
    fun <T> serialize(type: KType, obj: T): ByteArray
    fun <T> deserialize(type: KType, bytes: ByteArray): T
}

inline fun <reified T> Serializer.serialize(obj: T): ByteArray = serialize(typeOf<T>(), obj)
inline fun <reified T> Serializer.deserialize(bytes: ByteArray): T = deserialize(typeOf<T>(), bytes)