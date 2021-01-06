/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/11/2020
 */

package aragones.sergio.readercollection.repositories

import aragones.sergio.readercollection.models.responses.BookResponse
import aragones.sergio.readercollection.network.apiclient.BookAPIClient
import aragones.sergio.readercollection.utils.SharedPreferencesHandler
import io.reactivex.rxjava3.core.Maybe
import javax.inject.Inject

class BooksRepository @Inject constructor(
    private val sharedPreferencesHandler: SharedPreferencesHandler,
    private val bookAPIClient: BookAPIClient
) {

    //MARK: - Public properties

    val sortParam: String?
        get() = sharedPreferencesHandler.getSortParam()

    //MARK: - Public methods

    fun getBooks(format: String?, state: String?, isFavourite: Boolean?, sortParam: String?): Maybe<List<BookResponse>> {
        return bookAPIClient.getBooksObserver(format, state, isFavourite, sortParam)
    }
}