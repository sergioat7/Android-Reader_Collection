/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 29/12/2024
 */

package aragones.sergio.readercollection.data.local.di

import aragones.sergio.readercollection.data.local.SharedPreferencesHandler
import aragones.sergio.readercollection.data.local.UserLocalDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    @Singleton
    @Provides
    fun providesSharedPreferencesHandler() = SharedPreferencesHandler()
}

val storageModule = module {
    singleOf(::SharedPreferencesHandler)
    factoryOf(::UserLocalDataSource)
}