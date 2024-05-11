package net.ultragrav.knet.proxy

import kotlinx.coroutines.CoroutineDispatcher

data class ProxyCallHandlerConfig(val dispatcher: () -> CoroutineDispatcher)