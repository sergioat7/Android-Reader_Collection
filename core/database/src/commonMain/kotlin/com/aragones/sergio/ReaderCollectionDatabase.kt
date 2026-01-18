/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 29/3/2024
 */

package com.aragones.sergio

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import com.aragones.sergio.converters.ListConverter
import com.aragones.sergio.model.Book

@Database(
    entities = [Book::class],
    version = 4,
)
@TypeConverters(
    ListConverter::class,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class ReaderCollectionDatabase : RoomDatabase() {

    //region Public properties
    abstract fun bookDao(): BookDao
    //endregion
}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<ReaderCollectionDatabase> {
    override fun initialize(): ReaderCollectionDatabase
}