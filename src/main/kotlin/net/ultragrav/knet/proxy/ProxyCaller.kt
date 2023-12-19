package net.ultragrav.knet.proxy

import net.ultragrav.knet.packet.packets.PacketProxyCall

abstract class ProxyCaller {
    private val cachedProxies: MutableMap<Class<*>, Any> = HashMap()

    val callProvider = ProxyCallProviderImpl(this)

    abstract suspend fun sendCall(call: PacketProxyCall)
    abstract suspend fun handleCall(call: PacketProxyCall): ByteArray

    fun getProxy(clazz: Class<*>): Any {
        return cachedProxies.getOrPut(clazz) {
            ProxyFactory.createProxy(clazz, callProvider)
        }
    }

    inline fun <reified T : Any> getProxy(): T {
        return getProxy(T::class.java) as T
    }
}