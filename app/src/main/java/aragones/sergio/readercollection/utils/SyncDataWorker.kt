/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 24/6/2025
 */

package aragones.sergio.readercollection.utils

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.domain.di.IoScheduler
import aragones.sergio.readercollection.domain.di.MainScheduler
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import kotlinx.coroutines.rx3.rxCompletable

@HiltWorker
class SyncDataWorker @AssistedInject constructor(
    private val booksRepository: BooksRepository,
    private val userRepository: UserRepository,
    @IoScheduler private val ioScheduler: Scheduler,
    @MainScheduler private val mainScheduler: Scheduler,
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
) : Worker(context, workerParams) {

    companion object {
        const val WORK_NAME = "SyncDataWorker"
    }

    private val disposables = CompositeDisposable()

    override fun doWork(): Result {
        val userId = userRepository.userId
        if (userId.isEmpty()) return Result.failure()

        rxCompletable {
            booksRepository
                .syncBooks(userId)
        }.subscribeOn(ioScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onComplete = {},
                onError = {},
            ).addTo(disposables)

        return Result.success()
    }

    override fun onStopped() {
        super.onStopped()
        disposables.clear()
    }
}