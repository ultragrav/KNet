package net.ultragrav.knet

import net.ultragrav.knet.ProxiedInterface

@ProxiedInterface
interface TestInterface {
    suspend fun test(arg: String): String
}