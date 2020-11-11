/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 20/10/2020
 */

package aragones.sergio.readercollection.injection

import android.content.Context
import android.content.SharedPreferences
import aragones.sergio.readercollection.utils.Constants
import dagger.Module
import dagger.Provides

@Module
class SharedPreferencesModule (private val context: Context?) {

    @Provides
    fun provideSharedPreferences(): SharedPreferences? = context?.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE)
}