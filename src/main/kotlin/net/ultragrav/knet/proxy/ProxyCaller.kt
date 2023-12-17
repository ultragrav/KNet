package net.ultragrav.knet.proxy

import net.ultragrav.knet.packet.packets.PacketProxyCall

interface ProxyCaller {
    val callProvider: ProxyCallProviderImpl

    suspend fun sendCall(call: PacketProxyCall)
    suspend fun handleCall(call: PacketProxyCall): ByteArray
}