/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 7/11/2020
 */

package aragones.sergio.readercollection.repositories

import aragones.sergio.readercollection.models.responses.GoogleBookListResponse
import aragones.sergio.readercollection.models.responses.GoogleBookResponse
import aragones.sergio.readercollection.network.apiclient.GoogleAPIClient
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class SearchRepository @Inject constructor(
    private val googleAPIClient: GoogleAPIClient
) {

    //MARK: - Public methods

    fun searchBooks(query: String?): Single<GoogleBookListResponse> {
        return googleAPIClient.searchGoogleBooksObserver(query)
    }

    fun getBook(volumeId: String): Single<GoogleBookResponse> {
        return googleAPIClient.getGoogleBookObserver(volumeId)
    }
}