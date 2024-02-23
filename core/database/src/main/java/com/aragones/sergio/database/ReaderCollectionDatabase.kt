/*
 * Copyright (c) 2021 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 5/1/2021
 */

package com.aragones.sergio.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aragones.sergio.data.business.BaseModel
import com.aragones.sergio.data.business.BookResponse
import com.aragones.sergio.database.daos.BookDao

@Database(
    entities = [BookResponse::class],
    version = 3
)
@TypeConverters(
    com.aragones.sergio.database.converters.ListConverter::class,
    com.aragones.sergio.database.converters.DateConverter::class
)
abstract class ReaderCollectionDatabase : RoomDatabase() {

    //region Public properties
    abstract fun bookDao(): BookDao
    //endregion

    companion object {

        //region Public methods
        fun <T> getDisabledContent(
            currentValues: List<BaseModel<T>>,
            newValues: List<BaseModel<T>>
        ): List<BaseModel<T>> {

            val disabledContent = arrayListOf<BaseModel<T>>()
            for (currentValue in currentValues) {

                if (newValues.firstOrNull { it.id == currentValue.id } == null) {
                    disabledContent.add(currentValue)
                }
            }
            return disabledContent
        }
        //endregion
    }
}