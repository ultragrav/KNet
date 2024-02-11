package net.ultragrav.knet

import kotlinx.coroutines.Dispatchers
import net.ultragrav.knet.proxy.KNetCallerContextElement
import net.ultragrav.knet.proxy.ProxyCaller
import net.ultragrav.knet.serialization.KJsonSerializer
import net.ultragrav.knet.serialization.Serializer
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration.Companion.minutes

object KNet {
    var defaultDispatcher = Dispatchers.Default
    var serializer: Serializer = KJsonSerializer
    var callTimeout = 5.minutes

    suspend inline fun caller(): ProxyCaller {
        return coroutineContext[KNetCallerContextElement.Key]?.caller ?: error("No caller found in context")
    }
}