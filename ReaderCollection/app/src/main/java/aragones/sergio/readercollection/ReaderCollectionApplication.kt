/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 20/10/2020
 */

package aragones.sergio.readercollection

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import aragones.sergio.readercollection.injection.AppComponent
import aragones.sergio.readercollection.injection.AppDatabaseModule
import aragones.sergio.readercollection.injection.DaggerAppComponent
import aragones.sergio.readercollection.injection.NetworkModule

class ReaderCollectionApplication : Application() {

    //region Static properties
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }
    //endregion

    //region Public properties
    lateinit var appComponent: AppComponent
    //endregion

    //region Lifecycle methods
    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent.builder()
            .appDatabaseModule(
                AppDatabaseModule(applicationContext)
            )
            .networkModule(NetworkModule())
            .build()

        context = applicationContext
    }
    //endregion
}