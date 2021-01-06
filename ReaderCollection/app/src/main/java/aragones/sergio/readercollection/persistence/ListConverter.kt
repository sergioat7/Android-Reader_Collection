/*
 * Copyright (c) 2021 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 6/1/2021
 */

package aragones.sergio.readercollection.persistence

import androidx.room.TypeConverter
import aragones.sergio.readercollection.models.responses.BookResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class ListConverter {

    private val gson = Gson()

    @TypeConverter
    fun stringToBookList(data: String?): List<BookResponse?>? {
        if (data == null) {
            return Collections.emptyList()
        }
        val listType =
            object : TypeToken<List<BookResponse?>?>() {}.type
        return gson.fromJson<List<BookResponse?>>(data, listType)
    }

    @TypeConverter
    fun bookListToString(books: List<BookResponse?>?): String? {
        return gson.toJson(books)
    }

    @TypeConverter
    fun stringToStringList(data: String?): List<String?>? {
        if (data == null) {
            return Collections.emptyList()
        }
        val listType =
            object : TypeToken<List<String?>?>() {}.type
        return gson.fromJson<List<String?>>(data, listType)
    }

    @TypeConverter
    fun stringListToString(elements: List<String?>?): String? {
        return gson.toJson(elements)
    }
}