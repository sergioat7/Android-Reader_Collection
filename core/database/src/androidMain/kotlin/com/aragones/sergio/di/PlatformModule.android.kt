/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 3/1/2026
 */

package com.aragones.sergio.di

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.AndroidSQLiteDriver
import com.aragones.sergio.ReaderCollectionDatabase
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.named
import org.koin.dsl.module

actual val platformModule = module {
    single<RoomDatabase.Builder<ReaderCollectionDatabase>> {
        Room
            .databaseBuilder(
                context = get(),
                klass = ReaderCollectionDatabase::class.java,
                name = get(named("database_name")),
            ).setDriver(AndroidSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
    }
}