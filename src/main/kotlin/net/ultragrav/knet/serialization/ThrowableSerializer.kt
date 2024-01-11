package net.ultragrav.knet.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

object ThrowableSerializer : KSerializer<Throwable> {
    private val delegate = ByteArraySerializer()
    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun deserialize(decoder: Decoder): Throwable {
        val ois = ObjectInputStream(ByteArrayInputStream(decoder.decodeSerializableValue(delegate)))
        val exception = ois.readObject() as Throwable
        ois.close()
        return exception
    }

    override fun serialize(encoder: Encoder, value: Throwable) {
        val baos = ByteArrayOutputStream()
        val oos = ObjectOutputStream(baos)
        oos.writeObject(value)
        oos.close()
        encoder.encodeSerializableValue(delegate, baos.toByteArray())
    }
}