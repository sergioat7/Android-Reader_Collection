/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/1/2026
 */

package com.aragones.sergio.di

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.aragones.sergio.ReaderCollectionDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.qualifier.named
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

actual val platformModule = module {
    single<RoomDatabase.Builder<ReaderCollectionDatabase>> {
        val dbName: String = get(named("database_name"))
        val dbFile = "${fileDirectory()}/$dbName"
        Room
            .databaseBuilder<ReaderCollectionDatabase>(dbFile)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun fileDirectory(): String {
    val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(documentDirectory).path!!
}