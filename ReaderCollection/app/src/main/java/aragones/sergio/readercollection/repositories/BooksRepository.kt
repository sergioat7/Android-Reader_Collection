/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/11/2020
 */

package aragones.sergio.readercollection.repositories

import androidx.sqlite.db.SimpleSQLiteQuery
import aragones.sergio.readercollection.models.responses.BookResponse
import aragones.sergio.readercollection.network.apiclient.BookAPIClient
import aragones.sergio.readercollection.persistence.AppDatabase
import aragones.sergio.readercollection.utils.Constants
import aragones.sergio.readercollection.utils.SharedPreferencesHandler
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

class BooksRepository @Inject constructor(
    private val sharedPreferencesHandler: SharedPreferencesHandler,
    private val bookAPIClient: BookAPIClient,
    private val database: AppDatabase
) {

    //MARK: - Private properties

    private val disposables = CompositeDisposable()

    //MARK: - Public properties

    val sortParam: String?
        get() = sharedPreferencesHandler.getSortParam()

    // MARK: - Lifecycle methods

    fun onDestroy() {
        disposables.clear()
    }

    //MARK: - Public methods

    fun loadBooks(): Completable {

        return Completable.create { emitter ->

            bookAPIClient.getBooksObserver(null, null, null, null).subscribeBy(
                onComplete = {
                    emitter.onComplete()
                },
                onSuccess = { newBooks ->
                    insertBooks(newBooks).subscribeBy(
                        onComplete = {
                            getBooks(null, null, null, null).subscribeBy(
                                onComplete = {
                                    emitter.onComplete()
                                },
                                onSuccess = { currentBooks ->

                                    val booksToRemove = Constants.getDisabledContent(currentBooks, newBooks) as List<BookResponse>
                                    deleteBooks(booksToRemove).subscribeBy(
                                        onComplete = {
                                            emitter.onComplete()
                                        },
                                        onError = {
                                            emitter.onError(it)
                                        })
                                        .addTo(disposables)
                                },
                                onError = {
                                    emitter.onError(it)
                                })
                                .addTo(disposables)
                        },
                        onError = {
                            emitter.onError(it)
                        })
                        .addTo(disposables)
                },
                onError = {
                    emitter.onError(it)
                })
                .addTo(disposables)
        }
    }

    fun insertBooks(books: List<BookResponse>): Completable {
        return database
            .bookDao()
            .insertBooks(books)
            .`as`(RxJavaBridge.toV3Completable())
            .subscribeOn(Constants.SUBSCRIBER_SCHEDULER)
            .observeOn(Constants.OBSERVER_SCHEDULER)
    }

    fun updateBooks(books: List<BookResponse>): Completable {
        return database
            .bookDao()
            .updateBooks(books)
            .`as`(RxJavaBridge.toV3Completable())
            .subscribeOn(Constants.SUBSCRIBER_SCHEDULER)
            .observeOn(Constants.OBSERVER_SCHEDULER)
    }

    fun deleteBooks(books: List<BookResponse>): Completable {
        return database
            .bookDao()
            .deleteBooks(books)
            .`as`(RxJavaBridge.toV3Completable())
            .subscribeOn(Constants.SUBSCRIBER_SCHEDULER)
            .observeOn(Constants.OBSERVER_SCHEDULER)
    }

    fun getBooks(format: String?, state: String?, isFavourite: Boolean?, sortParam: String?): Maybe<List<BookResponse>> {

        var queryString = "SELECT * FROM Book"
        var queryConditions = ""
        format?.let {
            queryConditions += "format == '${it}' AND "
        }
        state?.let {
            queryConditions += "state == '${it}' AND "
        }
        isFavourite?.let {
            queryConditions += if (it) "isFavourite == '1' AND " else "isFavourite == '0' AND "
        }

        if (queryConditions.isNotBlank()) queryString += " WHERE " + queryConditions.dropLast(5)

        sortParam?.let {
            queryString += " ORDER BY $it"
        }

        val query = SimpleSQLiteQuery(queryString)
        return database
            .bookDao()
            .getBooks(query)
            .`as`(RxJavaBridge.toV3Maybe())
            .subscribeOn(Constants.SUBSCRIBER_SCHEDULER)
            .observeOn(Constants.OBSERVER_SCHEDULER)
    }
}