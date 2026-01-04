/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 27/12/2025
 */

package aragones.sergio.readercollection.data.remote

import com.aragones.sergio.util.extensions.toDate
import com.aragones.sergio.util.extensions.toString
import java.util.Date
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object DateSerializer : KSerializer<Date?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: Date?) {
        val dateString = value.toString("dd/MM/yyyy") ?: value.toString()
        dateString?.let { encoder.encodeString(it) } ?: encoder.encodeNull()
    }

    override fun deserialize(decoder: Decoder): Date? {
        val dateString = decoder.decodeString()
        return dateString.toDate("dd/MM/yyyy") ?: dateString.toDate()
    }
}