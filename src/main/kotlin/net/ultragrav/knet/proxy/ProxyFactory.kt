package net.ultragrav.knet.proxy

import net.ultragrav.knet.ProxyCallHandler
import net.ultragrav.knet.ProxyCallProvider

object ProxyFactory {
    fun createProxy(clazz: Class<*>, callProvider: ProxyCallProvider): Any {
        val proxyClazz = Class.forName(clazz.name + "Proxy")
        val constructor = proxyClazz.getConstructor(ProxyCallProvider::class.java)
        return constructor.newInstance(callProvider)
    }

    fun <T> createHandler(clazz: Class<T>, implementation: T): ProxyCallHandler<T> {
        val handlerClazz = Class.forName(clazz.name + "CallHandler")
        val constructor = handlerClazz.getConstructor(clazz)
        return constructor.newInstance(implementation) as ProxyCallHandler<T>
    }
}