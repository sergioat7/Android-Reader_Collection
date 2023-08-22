/*
 * Copyright (c) 2023 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package aragones.sergio.readercollection.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import aragones.sergio.readercollection.database.ReaderCollectionDatabase
import aragones.sergio.readercollection.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("DROP TABLE Format")
            database.execSQL("DROP TABLE State")
        }
    }

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): ReaderCollectionDatabase {

        return Room
            .databaseBuilder(
                context.applicationContext,
                ReaderCollectionDatabase::class.java,
                Constants.DATABASE_NAME
            )
            .addMigrations(MIGRATION_1_2)
            .build()
    }
}