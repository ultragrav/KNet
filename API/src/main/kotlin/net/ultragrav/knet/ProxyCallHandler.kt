package net.ultragrav.knet

interface ProxyCallHandler<T> {
    suspend fun callProxyFunction(functionName: String, args: Array<ByteArray>): ByteArray
}