/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 7/11/2020
 */

package aragones.sergio.readercollection.domain

import aragones.sergio.readercollection.data.remote.ApiManager
import aragones.sergio.readercollection.data.remote.services.GoogleApiService
import aragones.sergio.readercollection.domain.base.BaseRepository
import com.aragones.sergio.data.business.GoogleBookListResponse
import com.aragones.sergio.data.business.GoogleBookResponse
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class GoogleBookRepository @Inject constructor(
    private val api: GoogleApiService
) : BaseRepository() {

    //region Public methods
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
    //endregion
}