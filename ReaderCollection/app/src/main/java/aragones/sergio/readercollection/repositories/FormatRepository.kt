/*
 * Copyright (c) 2021 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 5/1/2021
 */

package aragones.sergio.readercollection.repositories

import aragones.sergio.readercollection.models.responses.FormatResponse
import aragones.sergio.readercollection.network.ApiManager
import aragones.sergio.readercollection.network.apiservice.FormatApiService
import aragones.sergio.readercollection.persistence.AppDatabase
import aragones.sergio.readercollection.repositories.base.BaseRepository
import aragones.sergio.readercollection.utils.SharedPreferencesHandler
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

class FormatRepository @Inject constructor(
    private val api: FormatApiService,
    private val database: AppDatabase,
    private val sharedPreferencesHandler: SharedPreferencesHandler
) : BaseRepository() {

    //region Public methods
    fun loadFormatsObserver(): Completable {

        return Completable.create { emitter ->

            getFormatsObserver().subscribeBy(
                onSuccess = { newFormats ->
                    insertFormatsDatabaseObserver(newFormats).subscribeBy(
                        onComplete = {
                            getFormatsDatabaseObserver().subscribeBy(
                                onSuccess = { currentFormats ->

                                    val formatsToRemove = AppDatabase.getDisabledContent(
                                        currentFormats,
                                        newFormats
                                    ) as List<FormatResponse>
                                    deleteFormatsDatabaseObserver(formatsToRemove).subscribeBy(
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
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }

    fun getFormatsDatabaseObserver(): Single<List<FormatResponse>> {

        return database
            .formatDao()
            .getFormatsObserver()
            .`as`(RxJavaBridge.toV3Single())
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }

    fun resetTableObserver(): Completable {

        return Completable.create { emitter ->

            getFormatsDatabaseObserver().subscribeBy(
                onSuccess = { formats ->
                    deleteFormatsDatabaseObserver(formats).subscribeBy(
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
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }
    //endregion

    //region Private methods
    private fun getFormatsObserver(): Single<List<FormatResponse>> {

        val headers: MutableMap<String, String> = HashMap()
        headers[ApiManager.ACCEPT_LANGUAGE_HEADER] = sharedPreferencesHandler.getLanguage()
        return api
            .getFormats(headers)
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }

    private fun insertFormatsDatabaseObserver(formats: List<FormatResponse>): Completable {
        return database
            .formatDao()
            .insertFormatsObserver(formats)
            .`as`(RxJavaBridge.toV3Completable())
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }

    private fun deleteFormatsDatabaseObserver(formats: List<FormatResponse>): Completable {
        return database
            .formatDao()
            .deleteFormatsObserver(formats)
            .`as`(RxJavaBridge.toV3Completable())
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }
    //endregion
}