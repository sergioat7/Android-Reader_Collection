/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 2/1/2022
 */

package aragones.sergio.readercollection.injection.modules

import aragones.sergio.readercollection.network.ApiManager
import aragones.sergio.readercollection.network.apiservice.*
import dagger.Module
import dagger.Provides

@Module
class NetworkModule {
    @Provides
    fun providesBookApiService(): BookApiService = ApiManager.getService(
        ApiManager.BASE_ENDPOINT
    )

    @Provides
    fun providesFormatApiService(): FormatApiService = ApiManager.getService(
        ApiManager.BASE_ENDPOINT
    )

    @Provides
    fun providesGoogleApiService(): GoogleApiService = ApiManager.getService(
        ApiManager.BASE_GOOGLE_ENDPOINT
    )

    @Provides
    fun providesStateApiService(): StateApiService = ApiManager.getService(
        ApiManager.BASE_ENDPOINT
    )

    @Provides
    fun providesUserApiService(): UserApiService = ApiManager.getService(
        ApiManager.BASE_ENDPOINT
    )
}