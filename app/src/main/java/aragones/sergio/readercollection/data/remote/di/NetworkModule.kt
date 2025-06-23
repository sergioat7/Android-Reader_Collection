/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package aragones.sergio.readercollection.data.remote.di

import aragones.sergio.readercollection.data.remote.ApiManager
import aragones.sergio.readercollection.data.remote.services.BookApiService
import aragones.sergio.readercollection.data.remote.services.GoogleApiService
import aragones.sergio.readercollection.data.remote.services.UserApiService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
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

    @Singleton
    @Provides
    fun providesFirebaseRemoteConfig(): FirebaseRemoteConfig {
        val remoteConfig = Firebase.remoteConfig
        remoteConfig.setConfigSettingsAsync(
            remoteConfigSettings {
                minimumFetchIntervalInSeconds = 3600
            },
        )
        return remoteConfig
    }

    @Singleton
    @Provides
    fun providesFirebaseAuth(): FirebaseAuth = Firebase.auth

    @Singleton
    @Provides
    fun providesFirebaseFirestore(): FirebaseFirestore = Firebase.firestore
}