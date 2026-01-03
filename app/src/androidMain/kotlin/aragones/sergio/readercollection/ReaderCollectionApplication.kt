/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 20/10/2020
 */

package aragones.sergio.readercollection

import android.app.Application
import android.os.StrictMode
import aragones.sergio.readercollection.presentation.di.activityModule
import aragones.sergio.readercollection.presentation.di.presentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin

class ReaderCollectionApplication : Application() {

    //region Lifecycle methods
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@ReaderCollectionApplication)
            workManagerFactory()
            modules(
                activityModule,
                presentationModule,
            )
        }

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
}