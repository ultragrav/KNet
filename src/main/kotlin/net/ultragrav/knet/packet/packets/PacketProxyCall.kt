package net.ultragrav.knet.packet.packets

import kotlinx.serialization.Serializable

@Serializable
class PacketProxyCall(val id: Long, val className: String, val functionName: String, val args: Array<ByteArray>)