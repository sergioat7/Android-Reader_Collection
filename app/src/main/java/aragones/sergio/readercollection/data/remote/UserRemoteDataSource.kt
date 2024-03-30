/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 30/3/2024
 */

package aragones.sergio.readercollection.data.remote

import aragones.sergio.readercollection.data.remote.di.IoDispatcher
import aragones.sergio.readercollection.data.remote.model.ErrorResponse
import aragones.sergio.readercollection.data.remote.services.UserApiService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import javax.inject.Inject

class UserRemoteDataSource @Inject constructor(
    private val api: UserApiService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    //region Private properties
    private val externalScope = CoroutineScope(Job() + ioDispatcher)
    //endregion

    //region Public methods
    fun login(
        username: String,
        password: String,
        success: (String) -> Unit,
        failure: (ErrorResponse) -> Unit
    ) {
//        externalScope.launch {
//
//            val body = LoginCredentials(username, password)
//            try {
//                when (val response = ApiManager.validateResponse(api.login(body))) {
//                    is RequestResult.JsonSuccess -> success(response.body.token)
//                    is RequestResult.Failure -> failure(response.error)
//                    else -> failure(ErrorResponse(Constants.EMPTY_VALUE, R.string.error_server))
//                }
//            } catch (e: Exception) {
//                failure(ErrorResponse(Constants.EMPTY_VALUE, R.string.error_server))
//            }
//        }
        success("-")
    }

    fun logout() {
//        externalScope.launch {
//
//            try {
//                api.logout()
//            } catch (e: Exception) {
//                println(e)
//            }
//        }
    }

    fun register(
        username: String,
        password: String,
        success: () -> Unit,
        failure: (ErrorResponse) -> Unit
    ) {
//        externalScope.launch {
//
//            val body = LoginCredentials(username, password)
//            try {
//                when (val response = ApiManager.validateResponse(api.register(body))) {
//                    is RequestResult.Success -> success()
//                    is RequestResult.Failure -> failure(response.error)
//                    else -> failure(ErrorResponse("", R.string.error_server))
//                }
//            } catch (e: Exception) {
//                failure(ErrorResponse("", R.string.error_server))
//            }
//        }
        success()
    }

    fun updatePassword(password: String, success: () -> Unit, failure: (ErrorResponse) -> Unit) {
//        externalScope.launch {
//
//            try {
//                val body = NewPassword(password)
//                when (val response = ApiManager.validateResponse(api.updatePassword(body))) {
//                    is RequestResult.Success -> success()
//                    is RequestResult.Failure -> failure(response.error)
//                    else -> failure(ErrorResponse(Constants.EMPTY_VALUE, R.string.error_server))
//                }
//            } catch (e: Exception) {
//                failure(ErrorResponse(Constants.EMPTY_VALUE, R.string.error_server))
//            }
//        }
        success()
    }

    fun deleteUser(success: () -> Unit, failure: (ErrorResponse) -> Unit) {
//        externalScope.launch {
//
//            try {
//                when (val response = ApiManager.validateResponse(api.deleteUser())) {
//                    is RequestResult.Success -> success()
//                    is RequestResult.Failure -> failure(response.error)
//                    else -> failure(ErrorResponse(Constants.EMPTY_VALUE, R.string.error_server))
//                }
//            } catch (e: Exception) {
//                failure(ErrorResponse(Constants.EMPTY_VALUE, R.string.error_server))
//            }
//        }
        success()
    }
    //endregion
}