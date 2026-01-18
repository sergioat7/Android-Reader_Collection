/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 6/1/2026
 */

package aragones.sergio.readercollection.data.local.di

import aragones.sergio.readercollection.data.local.AppInfoProvider
import aragones.sergio.readercollection.data.local.SharedPreferencesProvider
import aragones.sergio.readercollection.data.local.SharedPreferencesProviderImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformModule = module {
    singleOf(::AppInfoProvider)
    singleOf(::SharedPreferencesProviderImpl) bind SharedPreferencesProvider::class
}