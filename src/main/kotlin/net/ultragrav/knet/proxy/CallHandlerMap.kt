package net.ultragrav.knet.proxy

import net.ultragrav.knet.ProxyCallHandler

class CallHandlerMap : ProxyRegistrar {
    private val map = mutableMapOf<String, ProxyCallHandler<*>>()

    override fun <T> registerProxy(inter: Class<T>, proxy: ProxyCallHandler<T>) {
        map[inter.name] = proxy
    }

    suspend fun call(className: String, functionName: String, args: Array<ByteArray>): ByteArray {
        val handler = map[className] ?: throw IllegalArgumentException("No class registered with name $className")
        return handler.callProxyFunction(functionName, args)
    }
}