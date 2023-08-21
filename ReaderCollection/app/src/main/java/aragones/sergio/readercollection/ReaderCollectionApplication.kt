/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 20/10/2020
 */

package aragones.sergio.readercollection

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import aragones.sergio.readercollection.injection.AppComponent
import aragones.sergio.readercollection.database.di.DatabaseModule
import aragones.sergio.readercollection.injection.DaggerAppComponent
import aragones.sergio.readercollection.network.di.NetworkModule

class ReaderCollectionApplication : Application() {

    //region Static properties
    companion object {
        val context: Context
            get() = app.applicationContext
        private lateinit var app: ReaderCollectionApplication
    }
    //endregion

    //region Public properties
    lateinit var appComponent: AppComponent
    //endregion

    //region Lifecycle methods
    override fun onCreate() {
        super.onCreate()

        app = this

        appComponent = DaggerAppComponent.builder()
            .databaseModule(
                DatabaseModule(applicationContext)
            )
            .networkModule(NetworkModule())
            .build()
    }
    //endregion
}