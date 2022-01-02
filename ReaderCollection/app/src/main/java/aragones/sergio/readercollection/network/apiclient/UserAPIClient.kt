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
import aragones.sergio.readercollection.utils.SharedPreferencesHandler
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class UserAPIClient @Inject constructor(
    private val api: UserApiService,
    private val sharedPreferencesHandler: SharedPreferencesHandler
) {

    fun registerObserver(username: String, password: String): Completable {

        val headers: MutableMap<String, String> = HashMap()
        headers[ApiManager.ACCEPT_LANGUAGE_HEADER] = sharedPreferencesHandler.getLanguage()
        val body = LoginCredentials(username, password)
        return api
            .register(headers, body)
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }

    fun deleteUserObserver(): Completable {

        val headers: MutableMap<String, String> = HashMap()
        headers[ApiManager.ACCEPT_LANGUAGE_HEADER] = sharedPreferencesHandler.getLanguage()
        headers[ApiManager.AUTHORIZATION_HEADER] = sharedPreferencesHandler.getCredentials().token
        return api
            .deleteUser(headers)
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }

    fun loginObserver(username: String, password: String): Single<LoginResponse> {

        val headers: MutableMap<String, String> = HashMap()
        headers[ApiManager.ACCEPT_LANGUAGE_HEADER] = sharedPreferencesHandler.getLanguage()
        val body = LoginCredentials(username, password)
        return api
            .login(headers, body)
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }

    fun logoutObserver(): Completable {

        val headers: MutableMap<String, String> = HashMap()
        headers[ApiManager.ACCEPT_LANGUAGE_HEADER] = sharedPreferencesHandler.getLanguage()
        headers[ApiManager.AUTHORIZATION_HEADER] = sharedPreferencesHandler.getCredentials().token
        return api
            .logout(headers)
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }

    fun updatePasswordObserver(password: String): Completable {

        val headers: MutableMap<String, String> = HashMap()
        headers[ApiManager.ACCEPT_LANGUAGE_HEADER] = sharedPreferencesHandler.getLanguage()
        headers[ApiManager.AUTHORIZATION_HEADER] = sharedPreferencesHandler.getCredentials().token
        val body = NewPassword(password)
        return api
            .updatePassword(headers, body)
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }
}