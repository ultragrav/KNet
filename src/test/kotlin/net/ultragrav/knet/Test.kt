package net.ultragrav.knet

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.ultragrav.knet.proxy.registerProxy
import java.net.InetSocketAddress

val server = KNetServer(3500)
val client = KNetClient(InetSocketAddress("localhost", 3500))

suspend fun main() = coroutineScope {
    server.run()
    client.connect()

    server.registerProxy(TestInterfaceCallHandler(TestInterfaceImpl()))

    val proxy = TestInterfaceProxy(client.callProvider)

    println(proxy.test("World"))

    delay(1000)

    server.connected.first().channel.close().awaitKt()

    launch {
        delay(1000)

        println("Reconnecting")
        client.connect()
        println("Done")
    }

    println("Sending request")
    println(proxy.test("World"))

    server.stop()
}