/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 14/5/2025
 */

package aragones.sergio.readercollection.presentation.di

import android.app.Activity
import androidx.activity.ComponentActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
object ActivityModule {
    @Provides
    fun providesComponentActivity(activity: Activity): ComponentActivity =
        activity as ComponentActivity
}