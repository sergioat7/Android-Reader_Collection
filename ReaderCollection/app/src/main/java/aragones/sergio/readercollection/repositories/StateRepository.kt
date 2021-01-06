/*
 * Copyright (c) 2021 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 6/1/2021
 */

package aragones.sergio.readercollection.repositories

import aragones.sergio.readercollection.models.responses.StateResponse
import aragones.sergio.readercollection.network.apiclient.StateAPIClient
import aragones.sergio.readercollection.persistence.AppDatabase
import aragones.sergio.readercollection.utils.Constants
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

class StateRepository @Inject constructor(
    private val stateAPIClient: StateAPIClient,
    private val database: AppDatabase
) {

    //MARK: - Private properties

    private val disposables = CompositeDisposable()

    // MARK: - Lifecycle methods

    fun onDestroy() {
        disposables.clear()
    }

    //MARK: - Public methods

    fun loadStates(): Completable {

        return Completable.create { emitter ->

            stateAPIClient.getStatesObserver().subscribeBy(
                onSuccess = { newStates ->
                    insertStates(newStates)
                        .subscribeBy(
                            onComplete = {
                                getStates()
                                    .subscribeBy(
                                        onSuccess = { currentStates ->

                                            val statesToRemove = Constants.getDisabledContent(currentStates, newStates) as List<StateResponse>
                                            deleteStates(statesToRemove)
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

    fun insertStates(states: List<StateResponse>): Completable {
        return database
            .stateDao()
            .insertStates(states)
            .`as`(RxJavaBridge.toV3Completable())
            .subscribeOn(Constants.SUBSCRIBER_SCHEDULER)
            .observeOn(Constants.OBSERVER_SCHEDULER)
    }

    fun updateStates(states: List<StateResponse>): Completable {
        return database
            .stateDao()
            .updateStates(states)
            .`as`(RxJavaBridge.toV3Completable())
            .subscribeOn(Constants.SUBSCRIBER_SCHEDULER)
            .observeOn(Constants.OBSERVER_SCHEDULER)
    }

    fun deleteStates(states: List<StateResponse>): Completable {
        return database
            .stateDao()
            .deleteStates(states)
            .`as`(RxJavaBridge.toV3Completable())
            .subscribeOn(Constants.SUBSCRIBER_SCHEDULER)
            .observeOn(Constants.OBSERVER_SCHEDULER)
    }

    fun getStates(): Single<List<StateResponse>> {
        return database
            .stateDao()
            .getStates()
            .`as`(RxJavaBridge.toV3Single())
            .subscribeOn(Constants.SUBSCRIBER_SCHEDULER)
            .observeOn(Constants.OBSERVER_SCHEDULER)
    }
}