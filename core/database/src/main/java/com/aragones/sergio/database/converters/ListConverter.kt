/*
 * Copyright (c) 2021 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 6/1/2021
 */

package com.aragones.sergio.database.converters

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.util.Collections

class ListConverter {

    private val moshiAdapter = Moshi.Builder().build().adapter<List<String?>?>(
        Types.newParameterizedType(
            List::class.java,
            String::class.java
        )
    )

    @TypeConverter
    fun stringToStringList(data: String?): List<String?>? {

        if (data == null) {
            return Collections.emptyList()
        }
        return moshiAdapter.fromJson(data).orEmpty()
    }

    @TypeConverter
    fun stringListToString(elements: List<String?>?): String? {
        return moshiAdapter.toJson(elements)
    }
}