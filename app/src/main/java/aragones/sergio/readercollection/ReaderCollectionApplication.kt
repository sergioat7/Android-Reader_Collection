/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 20/10/2020
 */

package aragones.sergio.readercollection

import android.app.Application
import android.content.Context
import android.os.StrictMode
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ReaderCollectionApplication : Application(), Configuration.Provider {

    //region Static properties
    companion object {
        val context: Context
            get() = app.applicationContext
        private lateinit var app: ReaderCollectionApplication
    }
    //endregion

    //region Public properties
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    //endregion

    //region Lifecycle methods
    override fun onCreate() {
        super.onCreate()

        app = this

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy
                    .Builder()
                    .detectAll()
                    .penaltyFlashScreen()
                    .penaltyLog()
                    .build(),
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy
                    .Builder()
                    .detectAll()
                    .penaltyLog()
                    .build(),
            )
        }
    }
    //endregion

    //region Interface methods
    override val workManagerConfiguration: Configuration
        get() = Configuration
            .Builder()
            .setWorkerFactory(workerFactory)
            .build()
    //endregion
}