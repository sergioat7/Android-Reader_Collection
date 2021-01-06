/*
 * Copyright (c) 2021 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 5/1/2021
 */

package aragones.sergio.readercollection.injection.modules

import android.content.Context
import aragones.sergio.readercollection.persistence.AppDatabase
import dagger.Module
import dagger.Provides

@Module
class AppDatabaseModule (private val context: Context) {

    @Provides
    fun provideAppDatabase() = AppDatabase.getAppDatabase(context)
}