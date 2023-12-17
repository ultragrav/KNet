package net.ultragrav.knet.packet.e

import net.ultragrav.knet.ProxiedInterface
import java.util.*

@ProxiedInterface
interface TestInterface {
    suspend fun test(arg: String): String
}