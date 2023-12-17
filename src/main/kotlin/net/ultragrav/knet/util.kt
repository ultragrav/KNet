package net.ultragrav.knet

import io.netty.channel.ChannelFuture
import io.netty.util.concurrent.Future
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun <T> Future<T>.awaitKt(): T {
    return suspendCoroutine { continuation ->
        this.addListener {
            if (this.isSuccess) {
                continuation.resume(this.now)
            } else {
                continuation.resumeWithException(this.cause())
            }
        }
    }
}
suspend fun ChannelFuture.awaitKt(): ChannelFuture {
    return suspendCoroutine { continuation ->
        this.addListener {
            if (this.isSuccess) {
                continuation.resume(this)
            } else {
                continuation.resumeWithException(this.cause())
            }
        }
    }
}