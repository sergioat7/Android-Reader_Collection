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
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import aragones.sergio.readercollection.base.BaseModel
import aragones.sergio.readercollection.daos.BookDao
import aragones.sergio.readercollection.models.responses.BookResponse
import aragones.sergio.readercollection.utils.Constants

@Database(
    entities = [BookResponse::class],
    version = 2
)
@TypeConverters(ListConverter::class, DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    //region Public properties
    abstract fun bookDao(): BookDao
    //endregion

    companion object {

        //region Private properties
        private var instance: AppDatabase? = null
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE Format")
                database.execSQL("DROP TABLE State")
            }
        }
        //endregion

        //region Public methods
        fun getAppDatabase(context: Context): AppDatabase {

            if (instance == null) {
                synchronized(AppDatabase::class) {

                    instance = Room
                        .databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java,
                            Constants.DATABASE_NAME
                        )
                        .addMigrations(MIGRATION_1_2)
                        .build()
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