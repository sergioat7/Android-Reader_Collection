/*
 * Copyright (c) 2023 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package aragones.sergio.readercollection.data.remote.di

import aragones.sergio.readercollection.data.remote.ApiManager
import aragones.sergio.readercollection.data.remote.services.BookApiService
import aragones.sergio.readercollection.data.remote.services.GoogleApiService
import aragones.sergio.readercollection.data.remote.services.UserApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_ENDPOINT = "https://books-collection-services.herokuapp.com/"
    private const val BASE_GOOGLE_ENDPOINT = "https://www.googleapis.com/books/v1/"

    @Singleton
    @Provides
    fun providesBookApiService(): BookApiService = ApiManager.getService(BASE_ENDPOINT)

    @Singleton
    @Provides
    fun providesGoogleApiService(): GoogleApiService = ApiManager.getService(BASE_GOOGLE_ENDPOINT)

    @Singleton
    @Provides
    fun providesUserApiService(): UserApiService = ApiManager.getService(BASE_ENDPOINT)
}