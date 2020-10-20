/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 20/10/2020
 */

package aragones.sergio.readercollection.injection

import android.app.Application

class ReaderCollectionApplication: Application() {

    lateinit var sharedPreferencesComponent: SharedPreferencesComponent

    override fun onCreate() {
        super.onCreate()

        sharedPreferencesComponent = DaggerSharedPreferencesComponent.builder().sharedPreferencesModule(
            SharedPreferencesModule(applicationContext)
        ).build()
    }
}