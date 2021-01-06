/*
 * Copyright (c) 2021 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 5/1/2021
 */

package aragones.sergio.readercollection.repositories

import aragones.sergio.readercollection.models.responses.FormatResponse
import aragones.sergio.readercollection.network.apiclient.FormatAPIClient
import aragones.sergio.readercollection.persistence.AppDatabase
import aragones.sergio.readercollection.utils.Constants
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

class FormatRepository @Inject constructor(
    private val formatAPIClient: FormatAPIClient,
    private val database: AppDatabase
) {

    //MARK: - Private properties

    private val disposables = CompositeDisposable()

    // MARK: - Lifecycle methods

    fun onDestroy() {
        disposables.clear()
    }

    //MARK: - Public methods

    fun loadFormats(): Completable {

        return Completable.create { emitter ->

            formatAPIClient.getFormatsObserver().subscribeBy(
                onSuccess = { newFormats ->
                    insertFormats(newFormats)
                        .subscribeBy(
                            onComplete = {
                                getFormats()
                                    .subscribeBy(
                                        onSuccess = { currentFormats ->

                                            val formatsToRemove = Constants.getDisabledContent(currentFormats, newFormats) as List<FormatResponse>
                                            deleteFormats(formatsToRemove)
                                                .subscribe({
                                                    emitter.onComplete()
                                                }, {
                                                    emitter.onError(it)
                                                })
                                        },
                                        onError = {
                                            emitter.onError(it)
                                        })
                                    .addTo(disposables)
                            },
                            onError = {
                                emitter.onError(it)
                            })
                        .addTo(disposables)
                },
                onError = {
                    emitter.onError(it)
                })
                .addTo(disposables)
        }
            .subscribeOn(Constants.SUBSCRIBER_SCHEDULER)
            .observeOn(Constants.OBSERVER_SCHEDULER)
    }

    fun insertFormats(formats: List<FormatResponse>): Completable {
        return database
            .formatDao()
            .insertFormats(formats)
            .`as`(RxJavaBridge.toV3Completable())
            .subscribeOn(Constants.SUBSCRIBER_SCHEDULER)
            .observeOn(Constants.OBSERVER_SCHEDULER)
    }

    fun updateFormats(formats: List<FormatResponse>): Completable {
        return database
            .formatDao()
            .updateFormats(formats)
            .`as`(RxJavaBridge.toV3Completable())
            .subscribeOn(Constants.SUBSCRIBER_SCHEDULER)
            .observeOn(Constants.OBSERVER_SCHEDULER)
    }

    fun deleteFormats(formats: List<FormatResponse>): Completable {
        return database
            .formatDao()
            .deleteFormats(formats)
            .`as`(RxJavaBridge.toV3Completable())
            .subscribeOn(Constants.SUBSCRIBER_SCHEDULER)
            .observeOn(Constants.OBSERVER_SCHEDULER)
    }

    fun getFormats(): Single<List<FormatResponse>> {
        return database
            .formatDao()
            .getFormats()
            .`as`(RxJavaBridge.toV3Single())
            .subscribeOn(Constants.SUBSCRIBER_SCHEDULER)
            .observeOn(Constants.OBSERVER_SCHEDULER)
    }
}