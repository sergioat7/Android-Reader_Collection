/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 25/12/2023
 */

package aragones.sergio.readercollection.data.remote

import com.aragones.sergio.util.extensions.toDate
import com.aragones.sergio.util.extensions.toString
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.util.Date

class MoshiDateAdapter(private val format: String) {

    @FromJson
    fun fromJson(dateString: String): Date? = dateString.toDate(format)

    @ToJson
    fun toJson(date: Date?): String? = date.toString(format)
}