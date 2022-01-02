/*
 * Copyright (c) 2021 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 9/1/2021
 */

package aragones.sergio.readercollection.repositories

import aragones.sergio.readercollection.models.responses.GoogleBookListResponse
import aragones.sergio.readercollection.models.responses.GoogleBookResponse
import aragones.sergio.readercollection.network.apiclient.GoogleApiClient
import aragones.sergio.readercollection.repositories.base.BaseRepository
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class GoogleBookRepository @Inject constructor(
    private val googleApiClient: GoogleApiClient
): BaseRepository() {

    //MARK: - Public methods

    fun searchBooksObserver(query: String, page: Int, order: String?): Single<GoogleBookListResponse> {
        return googleApiClient.searchGoogleBooksObserver(query, page, order)
    }

    fun getGoogleBookObserver(volumeId: String): Single<GoogleBookResponse> {
        return googleApiClient.getGoogleBookObserver(volumeId)
    }
}