package net.ultragrav.knet

interface ProxyCallProvider {
    suspend fun callProxyFunction(interfaceName: String, functionName: String, args: Array<ByteArray>): ByteArray
}