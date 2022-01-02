/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.network.apiclient

import aragones.sergio.readercollection.models.requests.FavouriteBook
import aragones.sergio.readercollection.models.responses.BookResponse
import aragones.sergio.readercollection.network.ApiManager
import aragones.sergio.readercollection.network.apiservice.BookApiService
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class BookApiClient @Inject constructor(
    private val api: BookApiService
) {

    fun getBooksObserver(): Maybe<List<BookResponse>> {

        return api
            .getBooks()
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }

    fun createBookObserver(book: BookResponse): Completable {

        return api
            .createBook(book)
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }

    fun setBookObserver(book: BookResponse): Single<BookResponse> {

        return api
            .setBook(book.id, book)
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }

    fun deleteBookObserver(googleId: String): Completable {

        return api
            .deleteBook(googleId)
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }

    fun setFavouriteBookObserver(googleId: String, isFavourite: Boolean): Single<BookResponse> {

        return api
            .setFavouriteBook(googleId, FavouriteBook(isFavourite))
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }
}