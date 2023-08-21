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

@Module
class NetworkModule {

    @Provides
    fun providesBookApiService(): BookApiService = ApiManager.getService(
        ApiManager.BASE_ENDPOINT
    )

    @Provides
    fun providesGoogleApiService(): GoogleApiService = ApiManager.getService(
        ApiManager.BASE_GOOGLE_ENDPOINT
    )

    @Provides
    fun providesUserApiService(): UserApiService = ApiManager.getService(
        ApiManager.BASE_ENDPOINT
    )
}