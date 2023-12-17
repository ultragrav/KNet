package net.ultragrav.knet.proxy

import net.ultragrav.knet.ProxyCallHandler

interface ProxyRegistrar {
    fun <T> registerProxy(inter: Class<T>, proxy: ProxyCallHandler<T>)
}

inline fun <reified T> ProxyRegistrar.registerProxy(proxy: ProxyCallHandler<T>) {
    registerProxy(T::class.java, proxy)
}