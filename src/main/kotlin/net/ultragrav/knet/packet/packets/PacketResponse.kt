package net.ultragrav.knet.packet.packets

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
class PacketResponse(
    val id: Long,
    val state: Byte,
    val returnValue: ByteArray,
    val exception: @Contextual Throwable? // TODO: Serializer for stacktrace
) {
    constructor(id: Long, returnValue: ByteArray) : this(id, STATE_SUCCESS, returnValue, null)
    constructor(id: Long, exception: Throwable) : this(id, STATE_EXCEPTION, ByteArray(0), exception)

    companion object {
        const val STATE_SUCCESS = 0.toByte()
        const val STATE_EXCEPTION = 1.toByte()
    }
}