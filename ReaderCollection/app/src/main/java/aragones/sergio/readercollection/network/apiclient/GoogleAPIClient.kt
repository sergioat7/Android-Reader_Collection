/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.network.apiclient

import aragones.sergio.readercollection.models.responses.GoogleBookListResponse
import aragones.sergio.readercollection.models.responses.GoogleBookResponse
import aragones.sergio.readercollection.network.apiservice.GooglePIService
import aragones.sergio.readercollection.utils.Constants
import io.reactivex.rxjava3.core.Single

class GoogleAPIClient() {

    private val api = APIClient.googleRetrofit.create(GooglePIService::class.java)

    fun searchGoogleBooksObserver(query: String?): Single<GoogleBookListResponse> {

        val params: MutableMap<String, String> = java.util.HashMap()
        if (query != null) {
            params[Constants.SEARCH_PARAM] = query
        }
        return api.searchGoogleBooks(params).subscribeOn(Constants.SUBSCRIBER_SCHEDULER).observeOn(Constants.OBSERVER_SCHEDULER)
    }

    fun getGoogleBookObserver(volumeId: String): Single<GoogleBookResponse> {
        return api.getGoogleBook(volumeId).subscribeOn(Constants.SUBSCRIBER_SCHEDULER).observeOn(Constants.OBSERVER_SCHEDULER)
    }
}