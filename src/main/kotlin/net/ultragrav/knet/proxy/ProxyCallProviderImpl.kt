package net.ultragrav.knet.proxy

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import net.ultragrav.knet.ProxyCallProvider
import net.ultragrav.knet.packet.packets.PacketProxyCall
import net.ultragrav.knet.packet.packets.PacketResponse
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalSerializationApi::class)
class ProxyCallProviderImpl(private val caller: ProxyCaller) : ProxyCallProvider {
    private val id = AtomicLong(0)

    // TODO: Handle cleanup in case of disconnect
    private val responseHandlers = mutableMapOf<Long, Continuation<ByteArray>>()

    override suspend fun callProxyFunction(
        interfaceName: String,
        functionName: String,
        args: Array<ByteArray>
    ): ByteArray = withContext(Dispatchers.IO) {
        val id = id.incrementAndGet()

        val call = PacketProxyCall(id, interfaceName, functionName, args)
        caller.sendCall(call)

        return@withContext suspendCancellableCoroutine { cont ->
            responseHandlers[id] = cont
            cont.invokeOnCancellation {
                responseHandlers.remove(id)
            }
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