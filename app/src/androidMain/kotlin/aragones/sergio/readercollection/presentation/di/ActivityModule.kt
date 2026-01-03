/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 14/5/2025
 */

package aragones.sergio.readercollection.presentation.di

import aragones.sergio.readercollection.domain.di.DispatchersName
import aragones.sergio.readercollection.presentation.navigation.Navigator
import aragones.sergio.readercollection.presentation.navigation.NavigatorImpl
import aragones.sergio.readercollection.utils.InAppUpdateService
import org.koin.androidx.scope.dsl.activityScope
import org.koin.core.module.dsl.scopedOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

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
    activityScope {
        scopedOf(::NavigatorImpl) bind Navigator::class
    }
}