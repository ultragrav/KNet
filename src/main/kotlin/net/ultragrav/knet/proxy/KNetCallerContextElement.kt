package net.ultragrav.knet.proxy

import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class KNetCallerContextElement(val caller: ProxyCaller) : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<KNetCallerContextElement>
}