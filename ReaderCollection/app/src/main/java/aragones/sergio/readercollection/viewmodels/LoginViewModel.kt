/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/10/2020
 */

package aragones.sergio.readercollection.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.models.login.AuthData
import aragones.sergio.readercollection.models.login.LoginFormState
import aragones.sergio.readercollection.models.login.UserData
import aragones.sergio.readercollection.models.responses.ErrorResponse
import aragones.sergio.readercollection.network.ApiManager
import aragones.sergio.readercollection.repositories.BooksRepository
import aragones.sergio.readercollection.repositories.FormatRepository
import aragones.sergio.readercollection.repositories.StateRepository
import aragones.sergio.readercollection.repositories.UserRepository
import aragones.sergio.readercollection.utils.Constants
import aragones.sergio.readercollection.viewmodels.base.BaseViewModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

class LoginViewModel @Inject constructor(
    private val booksRepository: BooksRepository,
    private val formatRepository: FormatRepository,
    private val stateRepository: StateRepository,
    private val userRepository: UserRepository
): BaseViewModel() {

    //MARK: - Private properties

    private val _loginForm = MutableLiveData<LoginFormState>()
    private val _loginLoading = MutableLiveData<Boolean>()
    private val _loginError = MutableLiveData<ErrorResponse?>()

    //MARK: - Public properties

    val username: String = userRepository.username
    val loginFormState: LiveData<LoginFormState> = _loginForm
    val loginLoading: LiveData<Boolean> = _loginLoading
    val loginError: LiveData<ErrorResponse?> = _loginError

    // MARK: - Lifecycle methods

    override fun onDestroy() {
        super.onDestroy()

        booksRepository.onDestroy()
        formatRepository.onDestroy()
        stateRepository.onDestroy()
    }

    //MARK: - Public methods

    fun login(username: String, password: String) {

        _loginLoading.value = true
        userRepository.loginObserver(username, password).subscribeBy(
            onSuccess = {

                val userData = UserData(username, password, true)
                val authData = AuthData(it.token)
                loadContent(userData, authData)
            },
            onError = {

                _loginLoading.value = false
                _loginError.value = ApiManager.handleError(it)
                onDestroy()
            }
        ).addTo(disposables)
    }

    fun loginDataChanged(username: String, password: String) {

        var usernameError: Int? = null
        var passwordError: Int? = null
        var isDataValid = true

        if (!Constants.isUserNameValid(username)) {
            usernameError = R.string.invalid_username
            isDataValid = false
        }
        if (!Constants.isPasswordValid(password)) {
            passwordError = R.string.invalid_password
            isDataValid = false
        }
        _loginForm.value = LoginFormState(usernameError, passwordError, isDataValid)
    }

    //MARK: - Private methods

    private fun loadContent(userData: UserData, authData: AuthData) {

        var result = 0

        loadFormatsObserver().subscribeBy(
            onComplete = {

                result += 1
                if (result == 2) {

                    userRepository.storeLoginData(userData, authData)
                    loadBooks()
                }
            },
            onError = {

                _loginError.value = ErrorResponse("", R.string.error_database)
                onDestroy()
            }
        ).addTo(disposables)

        loadStatesObserver().subscribeBy(
            onComplete = {

                result += 1
                if (result == 2) {

                    userRepository.storeLoginData(userData, authData)
                    loadBooks()
                }
            },
            onError = {

                _loginError.value = ErrorResponse("", R.string.error_database)
                onDestroy()
            }
        ).addTo(disposables)
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
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
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
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }

    private fun loadBooks() {

        booksRepository.loadBooksObserver().subscribeBy(
            onComplete = {

                _loginLoading.value = false
                _loginError.value = null
            },
            onError = {

                _loginError.value = ErrorResponse("", R.string.error_database)
                onDestroy()
            }
        ).addTo(disposables)
    }
}