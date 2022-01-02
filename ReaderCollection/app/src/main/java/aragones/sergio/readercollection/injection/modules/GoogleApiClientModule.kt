/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 7/11/2020
 */

package aragones.sergio.readercollection.injection.modules

import aragones.sergio.readercollection.network.apiclient.GoogleAPIClient
import dagger.Module
import dagger.Provides

@Module
class GoogleApiClientModule {

    @Provides
    fun provideGoogleApiClient() = GoogleAPIClient()
}