/*
 * Copyright (c) 2021 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 9/1/2021
 */

package aragones.sergio.readercollection.repositories

import aragones.sergio.readercollection.models.responses.GoogleBookListResponse
import aragones.sergio.readercollection.models.responses.GoogleBookResponse
import aragones.sergio.readercollection.network.ApiManager
import aragones.sergio.readercollection.network.apiservice.GoogleApiService
import aragones.sergio.readercollection.repositories.base.BaseRepository
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class GoogleBookRepository @Inject constructor(
    private val api: GoogleApiService
) : BaseRepository() {

    //MARK: - Public methods

    fun searchBooksObserver(
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
        return api
            .searchGoogleBooks(params)
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }

    fun getBookObserver(volumeId: String): Single<GoogleBookResponse> {

        return api
            .getGoogleBook(volumeId)
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }
}