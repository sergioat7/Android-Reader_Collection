/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package aragones.sergio.readercollection.domain.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.named
import org.koin.dsl.module

@Module
@InstallIn(SingletonComponent::class)
object CoroutineDispatcherModule {

    @DefaultDispatcher
    @Provides
    fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @MainDispatcher
    @Provides
    fun providesMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    @IoDispatcher
    @Provides
    fun providesIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class DefaultDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class MainDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class IoDispatcher

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