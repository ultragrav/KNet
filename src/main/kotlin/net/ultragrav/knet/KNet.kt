package net.ultragrav.knet

import kotlinx.coroutines.Dispatchers
import net.ultragrav.knet.proxy.KNetCallerContextElement
import net.ultragrav.knet.proxy.ProxyCaller
import net.ultragrav.knet.serialization.ProtoBufSerializer
import net.ultragrav.knet.serialization.Serializer
import kotlin.coroutines.coroutineContext

object KNet {
    var defaultDispatcher = Dispatchers.Default
    var serializer: Serializer = ProtoBufSerializer

    suspend inline fun caller(): ProxyCaller {
        return coroutineContext[KNetCallerContextElement.Key]?.caller ?: error("No caller found in context")
    }
}