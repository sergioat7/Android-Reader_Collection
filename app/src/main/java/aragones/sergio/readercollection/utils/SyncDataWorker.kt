/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 24/6/2025
 */

package aragones.sergio.readercollection.utils

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.UserRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncDataWorker @AssistedInject constructor(
    private val booksRepository: BooksRepository,
    private val userRepository: UserRepository,
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "SyncDataWorker"
    }

    override suspend fun doWork(): Result {
        val userId = userRepository.userId
        if (userId.isEmpty()) return Result.failure()

        return booksRepository.syncBooks(userId).fold(
            onSuccess = {
                Result.success()
            },
            onFailure = {
                Result.failure()
            },
        )
    }
}