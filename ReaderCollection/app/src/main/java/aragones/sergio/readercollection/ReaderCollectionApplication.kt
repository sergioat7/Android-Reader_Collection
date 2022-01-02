/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 20/10/2020
 */

package aragones.sergio.readercollection

import android.app.Application
import aragones.sergio.readercollection.injection.components.AppComponent
import aragones.sergio.readercollection.injection.components.DaggerAppComponent
import aragones.sergio.readercollection.injection.modules.AppDatabaseModule
import aragones.sergio.readercollection.injection.modules.NetworkModule
import aragones.sergio.readercollection.injection.modules.SharedPreferencesModule

class ReaderCollectionApplication : Application() {

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
            .sharedPreferencesModule(
                SharedPreferencesModule(applicationContext)
            )
            .build()
    }
    //endregion
}