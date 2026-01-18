/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 2/1/2026
 */

package aragones.sergio.readercollection.presentation.di

import aragones.sergio.readercollection.domain.di.DispatchersName
import aragones.sergio.readercollection.presentation.navigation.Navigator
import aragones.sergio.readercollection.presentation.navigation.NavigatorImpl
import aragones.sergio.readercollection.utils.InAppUpdateService
import aragones.sergio.readercollection.utils.SyncDataWorker
import org.koin.androidx.scope.dsl.activityScope
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.scopedOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformModule = module {
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
    workerOf(::SyncDataWorker)
}