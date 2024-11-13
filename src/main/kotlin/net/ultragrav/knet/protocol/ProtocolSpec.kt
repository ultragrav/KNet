package net.ultragrav.knet.protocol

import kotlinx.serialization.Serializable

@Serializable
data class ProtocolSpec(
    val classes: List<ProtocolClass>
) {
    fun matches(other: ProtocolSpec): Boolean {
        if (classes.size != other.classes.size) return false
        val otherClassesMutable = other.classes.toMutableList()
        // Compare classes by name
        for (ownClass in classes) {
            val otherClass = otherClassesMutable.firstOrNull { it.name == ownClass.name }
            if (otherClass == null) return false
            if (!ownClass.matches(otherClass)) return false
            otherClassesMutable.remove(otherClass)
        }
        return otherClassesMutable.isEmpty()
    }

    companion object {
        fun get(): ProtocolSpec {
            return Class.forName("KNetSpec").getField("spec").get(null) as ProtocolSpec
        }
    }
}
