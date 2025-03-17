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
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("DROP TABLE Format")
            db.execSQL("DROP TABLE State")
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE Book ADD priority INTEGER NOT NULL DEFAULT -1")
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `New_Book` (`id` TEXT NOT NULL, `title` TEXT, `subtitle` TEXT, `authors` TEXT, `publisher` TEXT, `publishedDate` INTEGER, `readingDate` INTEGER, `description` TEXT, `summary` TEXT, `isbn` TEXT, `pageCount` INTEGER NOT NULL, `categories` TEXT, `averageRating` REAL NOT NULL, `ratingsCount` INTEGER NOT NULL, `rating` REAL NOT NULL, `thumbnail` TEXT, `image` TEXT, `format` TEXT, `state` TEXT, `priority` INTEGER NOT NULL, PRIMARY KEY(`id`))",
            )
            db.execSQL(
                "INSERT INTO New_Book SELECT `id`, `title`, `subtitle`, `authors`, `publisher`, `publishedDate`, `readingDate`, `description`, `summary`, `isbn`, `pageCount`, `categories`, `averageRating`, `ratingsCount`, `rating`, `thumbnail`, `image`, `format`, `state`, `priority` FROM Book",
            )
            db.execSQL("DROP TABLE Book")
            db.execSQL("ALTER TABLE New_Book RENAME TO book")
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
        .addMigrations(MIGRATION_3_4)
        .build()

    @Provides
    fun provideBookDao(database: ReaderCollectionDatabase): BookDao = database.bookDao()
}