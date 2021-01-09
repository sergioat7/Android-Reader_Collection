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
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

class BooksRepository @Inject constructor(
    private val bookAPIClient: BookAPIClient,
    private val database: AppDatabase,
    private val sharedPreferencesHandler: SharedPreferencesHandler
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
                    insertBooksDatabase(newBooks).subscribeBy(
                        onComplete = {
                            getBooks(null, null, null, null).subscribeBy(
                                onComplete = {
                                    emitter.onComplete()
                                },
                                onSuccess = { currentBooks ->

                                    val booksToRemove = Constants.getDisabledContent(currentBooks, newBooks) as List<BookResponse>
                                    deleteBooksDatabase(booksToRemove).subscribeBy(
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
            .getBooksObserver(query)
            .`as`(RxJavaBridge.toV3Maybe())
            .subscribeOn(Constants.SUBSCRIBER_SCHEDULER)
            .observeOn(Constants.OBSERVER_SCHEDULER)
    }

    fun getBook(googleId: String): Single<BookResponse> {
        return database
            .bookDao()
            .getBookObserver(googleId)
            .`as`(RxJavaBridge.toV3Single())
            .subscribeOn(Constants.SUBSCRIBER_SCHEDULER)
            .observeOn(Constants.OBSERVER_SCHEDULER)
    }

    fun createBook(book: BookResponse): Completable {

        return Completable.create { emitter ->

            bookAPIClient.createBookObserver(book).subscribeBy(
                onComplete = {

                    loadBooks().subscribeBy(
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
        }
    }

    fun updateBook(book: BookResponse): Single<BookResponse> {

        return Single.create { emitter ->

            bookAPIClient.setBookObserver(book).subscribeBy(
                onSuccess = {

                    updateBooksDatabase(listOf(book)).subscribeBy(
                        onComplete = {
                            emitter.onSuccess(book)
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

    fun deleteBook(googleId: String): Completable {

        return Completable.create { emitter ->

            bookAPIClient.deleteBookObserver(googleId).subscribeBy(
                onComplete = {

                    getBook(googleId).subscribeBy(
                        onSuccess = { book ->

                            deleteBooksDatabase(listOf(book)).subscribeBy(
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
        }
    }

    fun setFavouriteBook(googleId: String, isFavourite: Boolean): Single<BookResponse> {

        return Single.create { emitter ->

            bookAPIClient.setFavouriteBookObserver(googleId, isFavourite).subscribeBy(
                onSuccess = { book ->

                    updateBooksDatabase(listOf(book)).subscribeBy(
                        onComplete = {
                            emitter.onSuccess(book)
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

    //MARK: - Private methods

    private fun insertBooksDatabase(books: List<BookResponse>): Completable {
        return database
            .bookDao()
            .insertBooksObserver(books)
            .`as`(RxJavaBridge.toV3Completable())
            .subscribeOn(Constants.SUBSCRIBER_SCHEDULER)
            .observeOn(Constants.OBSERVER_SCHEDULER)
    }

    private fun updateBooksDatabase(books: List<BookResponse>): Completable {
        return database
            .bookDao()
            .updateBooksObserver(books)
            .`as`(RxJavaBridge.toV3Completable())
            .subscribeOn(Constants.SUBSCRIBER_SCHEDULER)
            .observeOn(Constants.OBSERVER_SCHEDULER)
    }

    private fun deleteBooksDatabase(books: List<BookResponse>): Completable {
        return database
            .bookDao()
            .deleteBooksObserver(books)
            .`as`(RxJavaBridge.toV3Completable())
            .subscribeOn(Constants.SUBSCRIBER_SCHEDULER)
            .observeOn(Constants.OBSERVER_SCHEDULER)
    }
}