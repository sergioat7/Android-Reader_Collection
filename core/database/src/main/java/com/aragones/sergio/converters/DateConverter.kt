/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 5/1/2021
 */

package com.aragones.sergio.converters

import androidx.room.TypeConverter
import java.util.Date

class DateConverter {

    @TypeConverter
    fun toDate(dateLong: Long?): Date? = if (dateLong == null) null else Date(dateLong)

    @TypeConverter
    fun fromDate(date: Date?): Long? = date?.time
}