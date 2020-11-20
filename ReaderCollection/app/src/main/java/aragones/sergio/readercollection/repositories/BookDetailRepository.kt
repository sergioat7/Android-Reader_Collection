/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 11/11/2020
 */

package aragones.sergio.readercollection.repositories

import aragones.sergio.readercollection.models.responses.BookResponse
import aragones.sergio.readercollection.models.responses.FormatResponse
import aragones.sergio.readercollection.models.responses.GoogleBookResponse
import aragones.sergio.readercollection.models.responses.StateResponse
import aragones.sergio.readercollection.network.apiclient.BookAPIClient
import aragones.sergio.readercollection.network.apiclient.FormatAPIClient
import aragones.sergio.readercollection.network.apiclient.GoogleAPIClient
import aragones.sergio.readercollection.network.apiclient.StateAPIClient
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class BookDetailRepository @Inject constructor(
    private val bookAPIClient: BookAPIClient,
    private val googleAPIClient: GoogleAPIClient,
    private val formatAPIClient: FormatAPIClient,
    private val stateAPIClient: StateAPIClient
) {

    //MARK: - Public methods

    fun getBook(googleId: String): Maybe<BookResponse> {
        return bookAPIClient.getBookObserver(googleId)
    }

    fun getGoogleBook(volumeId: String): Single<GoogleBookResponse> {
        return googleAPIClient.getGoogleBookObserver(volumeId)
    }

    fun getFormats(): Single<List<FormatResponse>> {
        return formatAPIClient.getFormatsObserver()
    }

    fun getStates(): Single<List<StateResponse>> {
        return stateAPIClient.getStatesObserver()
    }
}