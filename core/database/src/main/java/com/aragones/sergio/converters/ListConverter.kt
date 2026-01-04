/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 6/1/2021
 */

package com.aragones.sergio.converters

import androidx.room.TypeConverter
import java.util.Collections
import kotlinx.serialization.json.Json

class ListConverter {

    @TypeConverter
    fun stringToStringList(data: String?): List<String>? =
        data?.let { Json.decodeFromString<List<String>>(it) } ?: Collections.emptyList()

    @TypeConverter
    fun stringListToString(elements: List<String>?): String? =
        elements?.let { Json.encodeToString(it) }
}
