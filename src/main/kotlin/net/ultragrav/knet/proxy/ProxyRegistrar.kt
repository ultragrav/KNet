package net.ultragrav.knet.proxy

import net.ultragrav.knet.KNet
import net.ultragrav.knet.ProxiedInterface
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

@Suppress("UNCHECKED_CAST")
fun <T : Any> ProxyRegistrar.registerProxy(proxy: T, config: ProxyCallHandlerConfig = ProxyCallHandlerConfig { KNet.defaultDispatcher }) {
    fun findProxiedInterface(root: Class<*>): Class<*>? {
        if (root.isAnnotationPresent(ProxiedInterface::class.java)) return root
        return root.interfaces.firstNotNullOfOrNull { findProxiedInterface(it) }
            ?: root.superclass?.let { findProxiedInterface(it) }
    }
    registerProxy(findProxiedInterface(proxy.javaClass) as Class<in T>, proxy, config)
}