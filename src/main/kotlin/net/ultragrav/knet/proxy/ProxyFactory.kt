package net.ultragrav.knet.proxy

import net.ultragrav.knet.ProxyCallProvider

object ProxyFactory {
    fun createProxy(clazz: Class<*>, callProvider: ProxyCallProvider): Any {
        val proxyClazz = Class.forName(clazz.name + "Proxy")
        val constructor = proxyClazz.getConstructor(ProxyCallProvider::class.java)
        return constructor.newInstance(callProvider)
    }
}