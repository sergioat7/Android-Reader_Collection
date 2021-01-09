/*
 * Copyright (c) 2021 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 5/1/2021
 */

package aragones.sergio.readercollection.repositories

import aragones.sergio.readercollection.models.responses.FormatResponse
import aragones.sergio.readercollection.network.apiclient.FormatAPIClient
import aragones.sergio.readercollection.persistence.AppDatabase
import aragones.sergio.readercollection.repositories.base.BaseRepository
import aragones.sergio.readercollection.utils.Constants
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

class FormatRepository @Inject constructor(
    private val database: AppDatabase,
    private val formatAPIClient: FormatAPIClient
): BaseRepository() {

    //MARK: - Public methods

    fun loadFormatsObserver(): Completable {

        return Completable.create { emitter ->

            formatAPIClient.getFormatsObserver().subscribeBy(
                onSuccess = { newFormats ->
                    insertFormatsObserver(newFormats).subscribeBy(
                        onComplete = {
                            getFormatsObserver().subscribeBy(
                                onSuccess = { currentFormats ->

                                    val formatsToRemove = Constants.getDisabledContent(currentFormats, newFormats) as List<FormatResponse>
                                    deleteFormatsObserver(formatsToRemove).subscribeBy(
                                        onComplete = {
                                            emitter.onComplete()
                                        },
                                        onError = {
                                            emitter.onError(it)
                                        }
                                    ).addTo(disposables)
                                },
                                onError = {
                                    emitter.onError(it)
                                }
                            ).addTo(disposables)
                        },
                        onError = {
                            emitter.onError(it)
                        }
                    ).addTo(disposables)
                },
                onError = {
                    emitter.onError(it)
                }
            ).addTo(disposables)
        }
            .subscribeOn(Constants.SUBSCRIBER_SCHEDULER)
            .observeOn(Constants.OBSERVER_SCHEDULER)
    }

    fun getFormatsObserver(): Single<List<FormatResponse>> {
        return database
            .formatDao()
            .getFormatsObserver()
            .`as`(RxJavaBridge.toV3Single())
            .subscribeOn(Constants.SUBSCRIBER_SCHEDULER)
            .observeOn(Constants.OBSERVER_SCHEDULER)
    }

    fun resetTableObserver(): Completable {

        return Completable.create { emitter ->

            getFormatsObserver().subscribeBy(
                onSuccess = { formats ->
                    deleteFormatsObserver(formats).subscribeBy(
                        onComplete = {
                            emitter.onComplete()
                        },
                        onError = {
                            emitter.onError(it)
                        }
                    ).addTo(disposables)
                },
                onError = {
                    emitter.onError(it)
                }
            ).addTo(disposables)
        }
            .subscribeOn(Constants.SUBSCRIBER_SCHEDULER)
            .observeOn(Constants.OBSERVER_SCHEDULER)
    }

    //MARK: - Private methods

    private fun insertFormatsObserver(formats: List<FormatResponse>): Completable {
        return database
            .formatDao()
            .insertFormatsObserver(formats)
            .`as`(RxJavaBridge.toV3Completable())
            .subscribeOn(Constants.SUBSCRIBER_SCHEDULER)
            .observeOn(Constants.OBSERVER_SCHEDULER)
    }

    private fun deleteFormatsObserver(formats: List<FormatResponse>): Completable {
        return database
            .formatDao()
            .deleteFormatsObserver(formats)
            .`as`(RxJavaBridge.toV3Completable())
            .subscribeOn(Constants.SUBSCRIBER_SCHEDULER)
            .observeOn(Constants.OBSERVER_SCHEDULER)
    }
}