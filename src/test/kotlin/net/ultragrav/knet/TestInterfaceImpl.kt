package net.ultragrav.knet

class TestInterfaceImpl : TestInterface {
    override suspend fun test(arg: String): String {
        return "Hello, $arg!"
    }
}