/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 2/1/2022
 */

package aragones.sergio.readercollection.injection

import aragones.sergio.readercollection.network.ApiManager
import aragones.sergio.readercollection.network.BookApiService
import aragones.sergio.readercollection.network.GoogleApiService
import aragones.sergio.readercollection.network.UserApiService
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