/*
 * Copyright (c) 2021 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 10/1/2021
 */

package aragones.sergio.readercollection.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.models.responses.ErrorResponse
import aragones.sergio.readercollection.repositories.BooksRepository
import aragones.sergio.readercollection.repositories.FormatRepository
import aragones.sergio.readercollection.repositories.StateRepository
import aragones.sergio.readercollection.utils.Constants
import aragones.sergio.readercollection.viewmodels.base.BaseViewModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

class PopupSyncAppViewModel @Inject constructor(
    private val booksRepository: BooksRepository,
    private val formatRepository: FormatRepository,
    private val stateRepository: StateRepository
): BaseViewModel() {

    //MARK: - Private properties

    private val _loginError = MutableLiveData<ErrorResponse?>()

    //MARK: - Public properties

    val loginError: LiveData<ErrorResponse?> = _loginError

    // MARK: - Lifecycle methods

    override fun onDestroy() {
        super.onDestroy()

        booksRepository.onDestroy()
        formatRepository.onDestroy()
        stateRepository.onDestroy()
    }

    //MARK: - Public methods

    fun loadContent() {

        var result = 0

        loadFormatsObserver().subscribeBy(
            onComplete = {

                result += 1
                checkProgress(result)
            },
            onError = {

                _loginError.value = ErrorResponse("", R.string.error_database)
                onDestroy()
            }
        ).addTo(disposables)

        loadStatesObserver().subscribeBy(
            onComplete = {

                result += 1
                checkProgress(result)
            },
            onError = {

                _loginError.value = ErrorResponse("", R.string.error_database)
                onDestroy()
            }
        ).addTo(disposables)

        loadBooksObserver().subscribeBy(
            onComplete = {

                result += 1
                checkProgress(result)
            },
            onError = {

                _loginError.value = ErrorResponse("", R.string.error_database)
                onDestroy()
            }
        ).addTo(disposables)
    }

    //MARK: - Private methods

    private fun checkProgress(result: Int) {

        if (result == 3) {
            _loginError.value = null
        }
    }

    private fun loadFormatsObserver(): Completable {

        return Completable.create { emitter ->

            formatRepository.loadFormatsObserver().subscribeBy(
                onComplete = {
                    emitter.onComplete()
                },
                onError = {
                    emitter.onError(it)
                }
            ).addTo(disposables)
        }
            .subscribeOn(Constants.SUBSCRIBER_SCHEDULER)
            .observeOn(Constants.OBSERVER_SCHEDULER)
    }

    private fun loadStatesObserver(): Completable {

        return Completable.create { emitter ->

            stateRepository.loadStatesObserver().subscribeBy(
                onComplete = {
                    emitter.onComplete()
                },
                onError = {
                    emitter.onError(it)
                }
            ).addTo(disposables)
        }
            .subscribeOn(Constants.SUBSCRIBER_SCHEDULER)
            .observeOn(Constants.OBSERVER_SCHEDULER)
    }

    private fun loadBooksObserver(): Completable {

        return Completable.create { emitter ->

            booksRepository.loadBooksObserver().subscribeBy(
                onComplete = {
                    emitter.onComplete()
                },
                onError = {
                    emitter.onError(it)
                }
            ).addTo(disposables)
        }
            .subscribeOn(Constants.SUBSCRIBER_SCHEDULER)
            .observeOn(Constants.OBSERVER_SCHEDULER)
    }
}