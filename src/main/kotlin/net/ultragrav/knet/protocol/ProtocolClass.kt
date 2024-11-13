package net.ultragrav.knet.protocol

import kotlinx.serialization.Serializable

@Serializable
data class ProtocolClass(
    val name: String,
    val methods: List<ProtocolMethod>
) {
    fun matches(other: ProtocolClass): Boolean {
        if (methods.size != other.methods.size) return false
        val otherMethodsMutable = other.methods.toMutableList()
        // Compare methods by name
        for (ownMethod in methods) {
            val otherMethod = otherMethodsMutable.firstOrNull { it.name == ownMethod.name }
            if (otherMethod == null) return false
            if (ownMethod != otherMethod) return false
            otherMethodsMutable.remove(otherMethod)
        }
        return otherMethodsMutable.isEmpty()
    }
}
