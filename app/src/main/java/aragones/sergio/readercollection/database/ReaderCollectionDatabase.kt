/*
 * Copyright (c) 2021 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 5/1/2021
 */

package aragones.sergio.readercollection.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import aragones.sergio.readercollection.database.daos.BookDao
import aragones.sergio.readercollection.models.BookResponse
import aragones.sergio.readercollection.models.base.BaseModel

@Database(
    entities = [BookResponse::class],
    version = 3
)
@TypeConverters(ListConverter::class, DateConverter::class)
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