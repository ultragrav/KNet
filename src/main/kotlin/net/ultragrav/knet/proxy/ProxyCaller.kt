package net.ultragrav.knet.proxy

import net.ultragrav.knet.packet.PacketProxyCall

interface ProxyCaller {
    val callProvider: ProxyCallProviderImpl

    fun sendCall(call: PacketProxyCall)
    suspend fun handleCall(call: PacketProxyCall): ByteArray
}