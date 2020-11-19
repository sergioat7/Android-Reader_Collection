/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/11/2020
 */

package aragones.sergio.readercollection.repositories

import aragones.sergio.readercollection.models.responses.BookResponse
import aragones.sergio.readercollection.network.apiclient.BookAPIClient
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class BooksRepository @Inject constructor(
    private val bookAPIClient: BookAPIClient
) {

    //MARK: - Public methods

    fun getBooks(format: String?, state: String?, isFavourite: Boolean?): Single<List<BookResponse>> {
        return bookAPIClient.getBooksObserver(format, state, isFavourite)
    }
}