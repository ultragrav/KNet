package net.ultragrav.knet.proxy

import kotlinx.serialization.ExperimentalSerializationApi
import net.ultragrav.knet.ProxyCallProvider
import net.ultragrav.knet.packet.PacketProxyCall
import net.ultragrav.knet.packet.PacketResponse
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalSerializationApi::class)
class ProxyCallProviderImpl(val caller: ProxyCaller) : ProxyCallProvider {
    private val id = AtomicLong(0)

    // TODO: Handle cleanup in case of disconnect
    private val responseHandlers = mutableMapOf<Long, Continuation<ByteArray>>()

    override suspend fun callProxyFunction(interfaceName: String, functionName: String, args: Array<ByteArray>): ByteArray {
        val id = id.incrementAndGet()

        val call = PacketProxyCall(id, interfaceName, functionName, args)
        caller.sendCall(call)

        return suspendCoroutine { cont ->
            responseHandlers[id] = cont
        }
    }

    internal fun handleResponse(response: PacketResponse) {
        val continuation = responseHandlers.remove(response.id) ?: return
        when (response.state) {
            PacketResponse.STATE_SUCCESS -> continuation.resume(response.returnValue)
            PacketResponse.STATE_EXCEPTION -> continuation.resumeWithException(response.exception!!)
        }
    }
}