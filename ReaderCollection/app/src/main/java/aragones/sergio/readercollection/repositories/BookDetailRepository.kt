/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 11/11/2020
 */

package aragones.sergio.readercollection.repositories

import aragones.sergio.readercollection.models.responses.BookResponse
import aragones.sergio.readercollection.models.responses.GoogleBookResponse
import aragones.sergio.readercollection.network.apiclient.BookAPIClient
import aragones.sergio.readercollection.network.apiclient.GoogleAPIClient
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class BookDetailRepository @Inject constructor(
    private val bookAPIClient: BookAPIClient,
    private val googleAPIClient: GoogleAPIClient
) {

    //MARK: - Public methods

    fun getBook(googleId: String): Single<BookResponse> {
        return bookAPIClient.getBookObserver(googleId)
    }

    fun getGoogleBook(volumeId: String): Single<GoogleBookResponse> {
        return googleAPIClient.getGoogleBookObserver(volumeId)
    }
}