/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 29/12/2024
 */

package aragones.sergio.readercollection.domain.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Qualifier

@Module
@InstallIn(SingletonComponent::class)
object SchedulerModule {

    @MainScheduler
    @Provides
    fun providesMainScheduler(): Scheduler = AndroidSchedulers.mainThread()

    @IoScheduler
    @Provides
    fun providesIoScheduler(): Scheduler = Schedulers.io()
}

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class MainScheduler

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class IoScheduler