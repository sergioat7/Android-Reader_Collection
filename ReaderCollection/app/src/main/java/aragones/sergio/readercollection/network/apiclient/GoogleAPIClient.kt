/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.network.apiclient

import aragones.sergio.readercollection.models.responses.GoogleBookListResponse
import aragones.sergio.readercollection.models.responses.GoogleBookResponse
import aragones.sergio.readercollection.network.ApiManager
import aragones.sergio.readercollection.network.apiservice.GoogleApiService
import io.reactivex.rxjava3.core.Single

class GoogleAPIClient {

    private val api = ApiManager.googleRetrofit.create(GoogleApiService::class.java)

    fun searchGoogleBooksObserver(
        query: String,
        page: Int,
        order: String?
    ): Single<GoogleBookListResponse> {

        val params: MutableMap<String, String> = HashMap()
        params[ApiManager.SEARCH_PARAM] = query
        params[ApiManager.PAGE_PARAM] = ((page - 1) * ApiManager.RESULTS).toString()
        params[ApiManager.RESULTS_PARAM] = ApiManager.RESULTS.toString()
        if (order != null) {
            params[ApiManager.ORDER_PARAM] = order
        }
        return api.searchGoogleBooks(params).subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }

    fun getGoogleBookObserver(volumeId: String): Single<GoogleBookResponse> {
        return api.getGoogleBook(volumeId).subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }
}