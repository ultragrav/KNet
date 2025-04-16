# KNet
Kotlin networking made easy.

## Usage
Create an interface representing transactions, ensure all functions are suspending and all arguments are serializable using kotlinx serialization
```kotlin
@ProxiedInterface
interface TestInterface {
    suspend fun test(arg: String?): String
    suspend fun testList(arg: List<String>): String
}
```
The `ProxiedInterface` annotation generates two important classes, a Proxy and a CallHandler, ideally you never need to directly interact with these

Example usage
```kotlin
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

    server.stop()
}
```

