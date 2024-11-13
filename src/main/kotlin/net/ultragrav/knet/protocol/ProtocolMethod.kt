package net.ultragrav.knet.protocol

import kotlinx.serialization.Serializable

@Serializable
data class ProtocolMethod(val name: String, val argTypes: List<String>, val returnType: String)
