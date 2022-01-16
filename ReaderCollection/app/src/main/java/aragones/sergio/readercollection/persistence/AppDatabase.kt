/*
 * Copyright (c) 2021 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 5/1/2021
 */

package aragones.sergio.readercollection.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import aragones.sergio.readercollection.daos.BookDao
import aragones.sergio.readercollection.daos.FormatDao
import aragones.sergio.readercollection.daos.StateDao
import aragones.sergio.readercollection.base.BaseModel
import aragones.sergio.readercollection.models.responses.BookResponse
import aragones.sergio.readercollection.models.FormatResponse
import aragones.sergio.readercollection.models.StateResponse
import aragones.sergio.readercollection.utils.Constants

@Database(entities = [
    BookResponse::class,
    FormatResponse::class,
    StateResponse::class], version = 1)
@TypeConverters(ListConverter::class, DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao
    abstract fun formatDao(): FormatDao
    abstract fun stateDao(): StateDao

    companion object {

        //region Private properties
        private var instance: AppDatabase? = null
        //endregion

        //region Public methods
        fun getAppDatabase(context: Context): AppDatabase {

            if (instance == null) {
                synchronized(AppDatabase::class) {

                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        Constants.DATABASE_NAME
                    ).build()
                }
            }
            return instance!!
        }

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