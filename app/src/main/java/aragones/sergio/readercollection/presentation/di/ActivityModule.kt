/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 14/5/2025
 */

package aragones.sergio.readercollection.presentation.di

import android.app.Activity
import androidx.activity.ComponentActivity
import aragones.sergio.readercollection.domain.di.DispatchersName
import aragones.sergio.readercollection.utils.InAppUpdateService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import org.koin.androidx.scope.dsl.activityScope
import org.koin.core.qualifier.named
import org.koin.dsl.module

@Module
@InstallIn(ActivityComponent::class)
object ActivityModule {
    @Provides
    fun providesComponentActivity(activity: Activity): ComponentActivity =
        activity as ComponentActivity
}

val activityModule = module {
    activityScope {
        scoped {
            InAppUpdateService(
                activity = get(),
                userRepository = get(),
                ioDispatcher = get(named(DispatchersName.IO)),
            )
        }
    }
}