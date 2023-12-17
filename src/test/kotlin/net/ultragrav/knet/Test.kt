package net.ultragrav.knet

import kotlinx.coroutines.delay
import net.ultragrav.knet.proxy.registerProxy
import java.net.InetSocketAddress

val server = KNetServer(3500)
val client = KNetClient(InetSocketAddress("localhost", 3500))

suspend fun main() {
    server.run()
    client.connect()

    server.proxies.registerProxy(TestInterfaceCallHandler(TestInterfaceImpl()))

    val proxy = TestInterfaceProxy(client.callProvider)

    println(proxy.test("World"))

    delay(1000)

    client.disconnect()
    server.stop()
}