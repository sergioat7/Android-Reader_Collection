/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 17/10/2020
 */

package aragones.sergio.readercollection.network.apiclient

import aragones.sergio.readercollection.models.StateResponse
import aragones.sergio.readercollection.network.apiservice.StateAPIService
import aragones.sergio.readercollection.utils.Constants
import aragones.sergio.readercollection.utils.SharedPreferencesHandler
import io.reactivex.rxjava3.core.Single

class StateAPIClient(
    private val sharedPreferencesHandler: SharedPreferencesHandler
) {

    private val api = APIClient.retrofit.create(StateAPIService::class.java)

    fun getStatesObserver(): Single<List<StateResponse>> {

        val headers: MutableMap<String, String> = HashMap()
        headers[Constants.ACCEPT_LANGUAGE_HEADER] = sharedPreferencesHandler.getLanguage()
        return api.getStates(headers).subscribeOn(Constants.SUBSCRIBER_SCHEDULER).observeOn(Constants.OBSERVER_SCHEDULER)
    }
}