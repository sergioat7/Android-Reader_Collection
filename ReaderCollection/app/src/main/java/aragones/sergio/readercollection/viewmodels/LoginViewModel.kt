/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/10/2020
 */

package aragones.sergio.readercollection.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import aragones.sergio.readercollection.models.login.AuthData
import aragones.sergio.readercollection.models.login.UserData
import aragones.sergio.readercollection.models.responses.ErrorResponse
import aragones.sergio.readercollection.network.apiclient.APIClient
import aragones.sergio.readercollection.repositories.LoginRepository
import io.reactivex.rxjava3.kotlin.subscribeBy
import retrofit2.HttpException

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    //MARK: - Private properties

    private val _loginError = MutableLiveData<ErrorResponse>()

    //MARK: - Public properties

    val loginError: LiveData<ErrorResponse> = _loginError

    //MARK: - Public methods

    fun login(username: String, password: String) {

        loginRepository.login(username, password).subscribeBy(
            onSuccess = {

                val userData = UserData(username, password, false)
                val authData = AuthData(it.token)
                loginRepository.storeLoginData(userData, authData)
                _loginError.value = null
            },
            onError = { error ->

                if (error is HttpException) {
                    error.response()?.errorBody()?.let { errorBody ->

                        _loginError.value = APIClient.gson.fromJson(
                            errorBody.charStream(), ErrorResponse::class.java
                        )
                    }
                }
            }
        )
    }
}