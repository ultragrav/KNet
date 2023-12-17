package net.ultragrav.knet.proxy

import net.ultragrav.knet.ProxyCallHandler

class CallHandlerMap {
    private val map = mutableMapOf<String, ProxyCallHandler<*>>()

    fun <T> registerProxy(inter: Class<T>, proxy: ProxyCallHandler<T>) {
        map[inter.name] = proxy
    }
    inline fun <reified T> registerProxy(proxy: ProxyCallHandler<T>) {
        registerProxy(T::class.java, proxy)
    }

    suspend fun call(className: String, functionName: String, args: Array<ByteArray>): ByteArray {
        val handler = map[className] ?: throw IllegalArgumentException("No class registered with name $className")
        return handler.callProxyFunction(functionName, args)
    }
}