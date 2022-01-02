/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 17/10/2020
 */

package aragones.sergio.readercollection.network.apiclient

import aragones.sergio.readercollection.models.responses.StateResponse
import aragones.sergio.readercollection.network.ApiManager
import aragones.sergio.readercollection.network.apiservice.StateApiService
import aragones.sergio.readercollection.utils.SharedPreferencesHandler
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class StateAPIClient @Inject constructor(
    private val sharedPreferencesHandler: SharedPreferencesHandler
) {

    private val api = ApiManager.retrofit.create(StateApiService::class.java)

    fun getStatesObserver(): Single<List<StateResponse>> {

        val headers: MutableMap<String, String> = HashMap()
        headers[ApiManager.ACCEPT_LANGUAGE_HEADER] = sharedPreferencesHandler.getLanguage()
        return api.getStates(headers).subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }
}