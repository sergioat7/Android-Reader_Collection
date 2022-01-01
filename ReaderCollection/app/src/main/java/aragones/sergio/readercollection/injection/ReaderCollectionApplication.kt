/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 20/10/2020
 */

package aragones.sergio.readercollection.injection

import android.app.Application
import aragones.sergio.readercollection.injection.components.AppComponent
import aragones.sergio.readercollection.injection.components.BooksComponent
import aragones.sergio.readercollection.injection.components.DaggerAppComponent
import aragones.sergio.readercollection.injection.components.DaggerBooksComponent
import aragones.sergio.readercollection.injection.modules.AppDatabaseModule
import aragones.sergio.readercollection.injection.modules.GoogleAPIClientModule
import aragones.sergio.readercollection.injection.modules.SharedPreferencesModule

class ReaderCollectionApplication : Application() {

    //MARK: - Public properties

    lateinit var appComponent: AppComponent
    lateinit var booksComponent: BooksComponent

    //MARK: - Lifecycle methods

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent.builder()
            .sharedPreferencesModule(
                SharedPreferencesModule(applicationContext)
            )
            .appDatabaseModule(
                AppDatabaseModule(applicationContext)
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
    }
}