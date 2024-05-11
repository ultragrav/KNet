package net.ultragrav.knet.proxy

import kotlinx.coroutines.withContext
import net.ultragrav.knet.ProxyCallHandler
import java.util.concurrent.ConcurrentHashMap

class CallHandlerMap : ProxyRegistrar {

    private class Registration(val handler: ProxyCallHandler<*>, val config: ProxyCallHandlerConfig)

    private val map = ConcurrentHashMap<String, Registration>()

    override fun <T> registerProxy(inter: Class<T>, proxy: ProxyCallHandler<T>, config: ProxyCallHandlerConfig) {
        map[inter.name] = Registration(proxy, config)
    }

    suspend fun call(className: String, functionName: String, args: Array<ByteArray>): ByteArray {
        val registration = map[className] ?: throw NoSuchProxyException(className)
        val handler = registration.handler
        val config = registration.config
        return withContext(config.dispatcher()) {
            handler.callProxyFunction(functionName, args)
        }
    }
}