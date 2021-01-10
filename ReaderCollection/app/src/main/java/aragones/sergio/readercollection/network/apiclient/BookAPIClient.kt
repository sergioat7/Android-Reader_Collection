/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.network.apiclient

import aragones.sergio.readercollection.models.responses.BookResponse
import aragones.sergio.readercollection.models.requests.FavouriteBook
import aragones.sergio.readercollection.network.apiservice.BookAPIService
import aragones.sergio.readercollection.utils.Constants
import aragones.sergio.readercollection.utils.SharedPreferencesHandler
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class BookAPIClient @Inject constructor(
    private val sharedPreferencesHandler: SharedPreferencesHandler
) {

    private val api = APIClient.retrofit.create(BookAPIService::class.java)

    fun getBooksObserver(): Maybe<List<BookResponse>> {

        val headers: MutableMap<String, String> = HashMap()
        headers[Constants.ACCEPT_LANGUAGE_HEADER] = sharedPreferencesHandler.getLanguage()
        headers[Constants.AUTHORIZATION_HEADER] = sharedPreferencesHandler.getCredentials().token

        return api.getBooks(headers).subscribeOn(Constants.SUBSCRIBER_SCHEDULER).observeOn(Constants.OBSERVER_SCHEDULER)
    }

    fun createBookObserver(book: BookResponse): Completable {

        val headers: MutableMap<String, String> = HashMap()
        headers[Constants.ACCEPT_LANGUAGE_HEADER] = sharedPreferencesHandler.getLanguage()
        headers[Constants.AUTHORIZATION_HEADER] = sharedPreferencesHandler.getCredentials().token
        return api.createBook(headers, book).subscribeOn(Constants.SUBSCRIBER_SCHEDULER).observeOn(Constants.OBSERVER_SCHEDULER)
    }

    fun setBookObserver(book: BookResponse): Single<BookResponse> {

        val headers: MutableMap<String, String> = HashMap()
        headers[Constants.ACCEPT_LANGUAGE_HEADER] = sharedPreferencesHandler.getLanguage()
        headers[Constants.AUTHORIZATION_HEADER] = sharedPreferencesHandler.getCredentials().token
        return api.setBook(headers, book.id, book).subscribeOn(Constants.SUBSCRIBER_SCHEDULER).observeOn(Constants.OBSERVER_SCHEDULER)
    }

    fun deleteBookObserver(googleId: String): Completable {

        val headers: MutableMap<String, String> = HashMap()
        headers[Constants.ACCEPT_LANGUAGE_HEADER] = sharedPreferencesHandler.getLanguage()
        headers[Constants.AUTHORIZATION_HEADER] = sharedPreferencesHandler.getCredentials().token
        return api.deleteBook(headers, googleId).subscribeOn(Constants.SUBSCRIBER_SCHEDULER).observeOn(Constants.OBSERVER_SCHEDULER)
    }

    fun setFavouriteBookObserver(googleId: String, isFavourite: Boolean): Single<BookResponse> {

        val headers: MutableMap<String, String> = HashMap()
        headers[Constants.ACCEPT_LANGUAGE_HEADER] = sharedPreferencesHandler.getLanguage()
        headers[Constants.AUTHORIZATION_HEADER] = sharedPreferencesHandler.getCredentials().token
        val body = FavouriteBook(isFavourite)
        return api.setFavouriteBook(headers, googleId, body).subscribeOn(Constants.SUBSCRIBER_SCHEDULER).observeOn(Constants.OBSERVER_SCHEDULER)
    }
}