/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 27/12/2025
 */

package aragones.sergio.readercollection.data.remote

import com.aragones.sergio.util.extensions.toLocalDate
import com.aragones.sergio.util.extensions.toString
import kotlinx.datetime.LocalDate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object DateSerializer : KSerializer<LocalDate?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: LocalDate?) {
        val dateString = value.toString("dd/MM/yyyy") ?: value.toString()
        dateString?.let { encoder.encodeString(it) } ?: encoder.encodeNull()
    }

    override fun deserialize(decoder: Decoder): LocalDate? {
        val dateString = decoder.decodeString()
        return dateString.toLocalDate("dd/MM/yyyy") ?: dateString.toLocalDate()
    }
}