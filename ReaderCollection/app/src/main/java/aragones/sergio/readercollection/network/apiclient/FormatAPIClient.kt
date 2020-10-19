/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 17/10/2020
 */

package aragones.sergio.readercollection.network.apiclient

import aragones.sergio.readercollection.models.responses.FormatResponse
import aragones.sergio.readercollection.network.apiservice.FormatAPIService
import aragones.sergio.readercollection.utils.Constants
import aragones.sergio.readercollection.utils.SharedPreferencesHandler
import io.reactivex.rxjava3.core.Single

class FormatAPIClient(
    private val sharedPreferencesHandler: SharedPreferencesHandler
) {

    private val api = APIClient.retrofit.create(FormatAPIService::class.java)

    fun getFormatsObserver(): Single<List<FormatResponse>> {

        val headers: MutableMap<String, String> = HashMap()
        headers[Constants.ACCEPT_LANGUAGE_HEADER] = sharedPreferencesHandler.getLanguage()
        return api.getFormats(headers).subscribeOn(Constants.SUBSCRIBER_SCHEDULER).observeOn(Constants.OBSERVER_SCHEDULER)
    }
}