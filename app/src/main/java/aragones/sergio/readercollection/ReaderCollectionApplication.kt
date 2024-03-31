/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 20/10/2020
 */

package aragones.sergio.readercollection

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ReaderCollectionApplication : Application() {

    //region Static properties
    companion object {
        val context: Context
            get() = app.applicationContext
        private lateinit var app: ReaderCollectionApplication
    }
    //endregion

    //region Lifecycle methods
    override fun onCreate() {
        super.onCreate()

        app = this
    }
    //endregion
}