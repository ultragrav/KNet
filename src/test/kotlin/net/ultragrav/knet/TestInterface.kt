package net.ultragrav.knet

@ProxiedInterface
interface TestInterface {
    suspend fun test(arg: String): String
    suspend fun testList(arg: List<String>): String
}