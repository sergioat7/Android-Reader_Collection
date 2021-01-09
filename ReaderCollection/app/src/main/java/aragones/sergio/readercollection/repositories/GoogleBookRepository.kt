/*
 * Copyright (c) 2021 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 9/1/2021
 */

package aragones.sergio.readercollection.repositories

import aragones.sergio.readercollection.models.responses.GoogleBookListResponse
import aragones.sergio.readercollection.models.responses.GoogleBookResponse
import aragones.sergio.readercollection.network.apiclient.GoogleAPIClient
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class GoogleBookRepository @Inject constructor(
    private val googleAPIClient: GoogleAPIClient
) {

    //MARK: - Public methods

    fun searchBooks(query: String, page: Int, order: String?): Single<GoogleBookListResponse> {
        return googleAPIClient.searchGoogleBooksObserver(query, page, order)
    }

    fun getGoogleBook(volumeId: String): Single<GoogleBookResponse> {
        return googleAPIClient.getGoogleBookObserver(volumeId)
    }
}