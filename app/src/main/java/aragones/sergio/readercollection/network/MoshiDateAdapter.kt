/*
 * Copyright (c) 2023 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 25/12/2023
 */

package aragones.sergio.readercollection.network

import aragones.sergio.readercollection.extensions.toDate
import aragones.sergio.readercollection.extensions.toString
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.util.Date

class MoshiDateAdapter(private val format: String) {

    @FromJson
    fun fromJson(dateString: String): Date? {
        return dateString.toDate(format)
    }

    @ToJson
    fun toJson(date: Date?): String? {
        return date.toString(format)
    }
}