/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/11/2020
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

class BooksRepository @Inject constructor(
    private val bookAPIClient: BookAPIClient,
    private val formatAPIClient: FormatAPIClient,
    private val stateAPIClient: StateAPIClient
) {

    //MARK: - Public methods

    fun getBooks(format: String?, state: String?, isFavourite: Boolean?, sortParam: String?): Maybe<List<BookResponse>> {
        return bookAPIClient.getBooksObserver(format, state, isFavourite, sortParam)
    }

    fun getFormats(): Single<List<FormatResponse>> {
        return formatAPIClient.getFormatsObserver()
    }

    fun getStates(): Single<List<StateResponse>> {
        return stateAPIClient.getStatesObserver()
    }
}