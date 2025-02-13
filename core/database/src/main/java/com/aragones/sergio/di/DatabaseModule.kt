/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package com.aragones.sergio.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aragones.sergio.BookDao
import com.aragones.sergio.ReaderCollectionDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DATABASE_NAME = "ReaderCollection"

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("DROP TABLE Format")
            database.execSQL("DROP TABLE State")
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE Book ADD priority INTEGER NOT NULL DEFAULT -1")
        }
    }

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): ReaderCollectionDatabase = Room
        .databaseBuilder(
            context.applicationContext,
            ReaderCollectionDatabase::class.java,
            DATABASE_NAME,
        ).addMigrations(MIGRATION_1_2)
        .addMigrations(MIGRATION_2_3)
        .build()

    @Provides
    fun provideBookDao(database: ReaderCollectionDatabase): BookDao = database.bookDao()
}