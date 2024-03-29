/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 29/3/2024
 */

package aragones.sergio.readercollection.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import aragones.sergio.readercollection.data.local.converters.DateConverter
import aragones.sergio.readercollection.data.local.converters.ListConverter
import aragones.sergio.readercollection.data.local.model.BaseEntity
import aragones.sergio.readercollection.data.local.model.Book

@Database(
    entities = [Book::class],
    version = 3
)
@TypeConverters(
    ListConverter::class,
    DateConverter::class
)
abstract class ReaderCollectionDatabase : RoomDatabase() {

    //region Public properties
    abstract fun bookDao(): BookDao
    //endregion

    companion object {

        //region Public methods
        fun <T> getDisabledContent(
            currentValues: List<BaseEntity<T>>,
            newValues: List<BaseEntity<T>>
        ): List<BaseEntity<T>> {

            val disabledContent = arrayListOf<BaseEntity<T>>()
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