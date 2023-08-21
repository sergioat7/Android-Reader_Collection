/*
 * Copyright (c) 2023 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package aragones.sergio.readercollection.database.di

import android.content.Context
import aragones.sergio.readercollection.database.AppDatabase
import dagger.Module
import dagger.Provides

@Module
class AppDatabaseModule (private val context: Context) {

    @Provides
    fun provideAppDatabase() = AppDatabase.getAppDatabase(context)
}