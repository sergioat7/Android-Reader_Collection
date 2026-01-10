/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package aragones.sergio.readercollection.domain.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.qualifier.named
import org.koin.dsl.module

val coroutineDispatcherModule = module {
    factory<CoroutineDispatcher>(named(DispatchersName.DEFAULT)) { Dispatchers.Default }
    factory<CoroutineDispatcher>(named(DispatchersName.MAIN)) { Dispatchers.Main }
    factory<CoroutineDispatcher>(named(DispatchersName.IO)) { Dispatchers.IO }
}

enum class DispatchersName {
    DEFAULT,
    MAIN,
    IO,
}