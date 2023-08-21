/*
 * Copyright (c) 2023 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package aragones.sergio.readercollection.network.di

import aragones.sergio.readercollection.network.ApiManager
import aragones.sergio.readercollection.network.interfaces.BookApiService
import aragones.sergio.readercollection.network.interfaces.GoogleApiService
import aragones.sergio.readercollection.network.interfaces.UserApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun providesBookApiService(): BookApiService = ApiManager.getService(
        ApiManager.BASE_ENDPOINT
    )

    @Singleton
    @Provides
    fun providesGoogleApiService(): GoogleApiService = ApiManager.getService(
        ApiManager.BASE_GOOGLE_ENDPOINT
    )

    @Singleton
    @Provides
    fun providesUserApiService(): UserApiService = ApiManager.getService(
        ApiManager.BASE_ENDPOINT
    )
}