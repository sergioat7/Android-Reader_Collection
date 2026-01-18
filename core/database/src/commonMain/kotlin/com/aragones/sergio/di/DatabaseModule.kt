/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package com.aragones.sergio.di

import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.aragones.sergio.BookDao
import com.aragones.sergio.BooksLocalDataSource
import com.aragones.sergio.ReaderCollectionDatabase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val DATABASE_NAME = "ReaderCollection"

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE Format")
        connection.execSQL("DROP TABLE State")
    }
}

private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE Book ADD priority INTEGER NOT NULL DEFAULT -1")
    }
}

private val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `New_Book` (`id` TEXT NOT NULL, `title` TEXT, `subtitle` TEXT, `authors` TEXT, `publisher` TEXT, `publishedDate` INTEGER, `readingDate` INTEGER, `description` TEXT, `summary` TEXT, `isbn` TEXT, `pageCount` INTEGER NOT NULL, `categories` TEXT, `averageRating` REAL NOT NULL, `ratingsCount` INTEGER NOT NULL, `rating` REAL NOT NULL, `thumbnail` TEXT, `image` TEXT, `format` TEXT, `state` TEXT, `priority` INTEGER NOT NULL, PRIMARY KEY(`id`))",
        )
        connection.execSQL(
            "INSERT INTO New_Book SELECT `id`, `title`, `subtitle`, `authors`, `publisher`, `publishedDate`, `readingDate`, `description`, `summary`, `isbn`, `pageCount`, `categories`, `averageRating`, `ratingsCount`, `rating`, `thumbnail`, `image`, `format`, `state`, `priority` FROM Book",
        )
        connection.execSQL("DROP TABLE Book")
        connection.execSQL("ALTER TABLE New_Book RENAME TO book")
    }
}

val databaseModule = module {
    includes(platformModule)
    single<String>(named("database_name")) { DATABASE_NAME }
    single<ReaderCollectionDatabase> {
        val builder = get<RoomDatabase.Builder<ReaderCollectionDatabase>>()
        builder
            .addMigrations(MIGRATION_1_2)
            .addMigrations(MIGRATION_2_3)
            .addMigrations(MIGRATION_3_4)
            .build()
    }
    single<BookDao> { get<ReaderCollectionDatabase>().bookDao() }
    factoryOf(::BooksLocalDataSource)
}