package net.ultragrav.knet

import io.netty.channel.ChannelFuture
import io.netty.util.concurrent.Future
import kotlinx.coroutines.*
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.system.exitProcess

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