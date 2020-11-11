/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 20/10/2020
 */

package aragones.sergio.readercollection.injection

import android.app.Application

class ReaderCollectionApplication: Application() {

    //MARK: - Public properties

    lateinit var sharedPreferencesComponent: SharedPreferencesComponent
    lateinit var googleApiClientComponent: GoogleAPIClientComponent
    lateinit var booksComponent: BooksComponent

    //MARK: - Lifecycle methods

    override fun onCreate() {
        super.onCreate()

        sharedPreferencesComponent = DaggerSharedPreferencesComponent.builder().sharedPreferencesModule(
            SharedPreferencesModule(applicationContext)
        ).build()

        googleApiClientComponent = DaggerGoogleAPIClientComponent.builder().googleAPIClientModule(
            GoogleAPIClientModule()
        ).build()

        booksComponent = DaggerBooksComponent.builder().sharedPreferencesModule(
            SharedPreferencesModule(applicationContext)
        ).googleAPIClientModule(
            GoogleAPIClientModule()
        ).build()
    }
}