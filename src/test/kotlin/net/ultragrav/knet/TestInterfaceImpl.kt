package net.ultragrav.knet

class TestInterfaceImpl : TestInterface {
    override suspend fun test(arg: String?): String {
        return "Hello, $arg!"
    }

    override suspend fun testList(arg: List<String>): String {
        return "Hello, ${arg.joinToString()}"
    }
}