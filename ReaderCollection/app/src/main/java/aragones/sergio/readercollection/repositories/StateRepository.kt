/*
 * Copyright (c) 2021 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 6/1/2021
 */

package aragones.sergio.readercollection.repositories

import aragones.sergio.readercollection.models.responses.StateResponse
import aragones.sergio.readercollection.network.ApiManager
import aragones.sergio.readercollection.network.StateApiService
import aragones.sergio.readercollection.persistence.AppDatabase
import aragones.sergio.readercollection.base.BaseRepository
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

class StateRepository @Inject constructor(
    private val api: StateApiService,
    private val database: AppDatabase
) : BaseRepository() {

    //region Public methods
    fun loadStatesObserver(): Completable {

        return Completable.create { emitter ->

            api.getStates()
                .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
                .observeOn(ApiManager.OBSERVER_SCHEDULER)
                .subscribeBy(
                    onSuccess = { newStates ->
                        insertStatesDatabaseObserver(newStates).subscribeBy(
                            onComplete = {
                                getStatesDatabaseObserver().subscribeBy(
                                    onSuccess = { currentStates ->

                                        val statesToRemove = AppDatabase.getDisabledContent(
                                            currentStates,
                                            newStates
                                        ) as List<StateResponse>
                                        deleteStatesDatabaseObserver(statesToRemove).subscribeBy(
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

    fun getStatesDatabaseObserver(): Single<List<StateResponse>> {
        return database
            .stateDao()
            .getStatesObserver()
            .`as`(RxJavaBridge.toV3Single())
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }

    fun resetTableObserver(): Completable {

        return Completable.create { emitter ->

            getStatesDatabaseObserver().subscribeBy(
                onSuccess = { states ->
                    deleteStatesDatabaseObserver(states).subscribeBy(
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

    private fun insertStatesDatabaseObserver(states: List<StateResponse>): Completable {
        return database
            .stateDao()
            .insertStatesObserver(states)
            .`as`(RxJavaBridge.toV3Completable())
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }

    private fun deleteStatesDatabaseObserver(states: List<StateResponse>): Completable {
        return database
            .stateDao()
            .deleteStatesObserver(states)
            .`as`(RxJavaBridge.toV3Completable())
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }
    //endregion
}