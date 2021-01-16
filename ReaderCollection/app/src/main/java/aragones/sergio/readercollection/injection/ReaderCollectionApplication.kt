/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 20/10/2020
 */

package aragones.sergio.readercollection.injection

import android.app.Application
import aragones.sergio.readercollection.injection.components.*
import aragones.sergio.readercollection.injection.modules.AppDatabaseModule
import aragones.sergio.readercollection.injection.modules.GoogleAPIClientModule
import aragones.sergio.readercollection.injection.modules.SharedPreferencesModule

class ReaderCollectionApplication: Application() {

    //MARK: - Public properties

    lateinit var googleApiClientComponent: GoogleAPIClientComponent
    lateinit var booksComponent: BooksComponent
    lateinit var loginComponent: LoginComponent

    //MARK: - Lifecycle methods

    override fun onCreate() {
        super.onCreate()

        googleApiClientComponent = DaggerGoogleAPIClientComponent.builder()
            .googleAPIClientModule(
                GoogleAPIClientModule()
            )
            .build()

        booksComponent = DaggerBooksComponent.builder()
            .sharedPreferencesModule(
                SharedPreferencesModule(applicationContext)
            )
            .googleAPIClientModule(
                GoogleAPIClientModule()
            )
            .appDatabaseModule(
                AppDatabaseModule(applicationContext)
            )
            .build()

        loginComponent = DaggerLoginComponent.builder()
            .sharedPreferencesModule(
                SharedPreferencesModule(applicationContext)
            )
            .appDatabaseModule(
                AppDatabaseModule(applicationContext)
            )
            .build()
    }
}