/*
 * Copyright (c) 2023 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package aragones.sergio.readercollection.data.source

import aragones.sergio.readercollection.data.source.base.BaseRepository
import aragones.sergio.readercollection.network.ApiManager
import aragones.sergio.readercollection.network.interfaces.GoogleApiService
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