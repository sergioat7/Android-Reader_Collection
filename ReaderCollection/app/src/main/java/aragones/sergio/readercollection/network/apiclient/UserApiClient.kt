/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 17/10/2020
 */

package aragones.sergio.readercollection.network.apiclient

import aragones.sergio.readercollection.models.requests.LoginCredentials
import aragones.sergio.readercollection.models.requests.NewPassword
import aragones.sergio.readercollection.models.responses.LoginResponse
import aragones.sergio.readercollection.network.ApiManager
import aragones.sergio.readercollection.network.apiservice.UserApiService
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class UserApiClient @Inject constructor(
    private val api: UserApiService
) {

    fun registerObserver(username: String, password: String): Completable {

        return api
            .register(LoginCredentials(username, password))
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }

    fun deleteUserObserver(): Completable {

        return api
            .deleteUser()
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }

    fun loginObserver(username: String, password: String): Single<LoginResponse> {

        return api
            .login(LoginCredentials(username, password))
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }

    fun logoutObserver(): Completable {

        return api
            .logout()
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }

    fun updatePasswordObserver(password: String): Completable {

        return api
            .updatePassword(NewPassword(password))
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }
}