package net.ultragrav.knet.proxy

import net.ultragrav.knet.KNet
import net.ultragrav.knet.ProxyCallHandler

interface ProxyRegistrar {
    fun <T> registerProxy(inter: Class<T>, proxy: ProxyCallHandler<T>, config: ProxyCallHandlerConfig = ProxyCallHandlerConfig { KNet.defaultDispatcher })
    fun <T> registerProxy(inter: Class<T>, proxy: T, config: ProxyCallHandlerConfig = ProxyCallHandlerConfig { KNet.defaultDispatcher }) {
        registerProxy(inter, ProxyFactory.createHandler(inter, proxy), config)
    }
}

inline fun <reified T> ProxyRegistrar.registerProxy(proxy: ProxyCallHandler<T>) {
    registerProxy(T::class.java, proxy)
}
inline fun <reified T> ProxyRegistrar.registerProxy(proxy: T) {
    registerProxy(T::class.java, proxy)
}