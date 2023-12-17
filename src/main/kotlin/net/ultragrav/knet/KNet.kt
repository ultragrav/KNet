package net.ultragrav.knet

import kotlinx.coroutines.Dispatchers
import net.ultragrav.knet.serialization.ProtoBufSerializer
import net.ultragrav.knet.serialization.Serializer

object KNet {
    var defaultDispatcher = Dispatchers.Default
    var serializer: Serializer = ProtoBufSerializer
}