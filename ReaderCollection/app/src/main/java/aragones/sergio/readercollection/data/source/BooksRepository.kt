/*
 * Copyright (c) 2023 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package aragones.sergio.readercollection.data.source

import androidx.sqlite.db.SimpleSQLiteQuery
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.source.base.BaseRepository
import aragones.sergio.readercollection.data.source.di.MainDispatcher
import aragones.sergio.readercollection.models.BookResponse
import aragones.sergio.readercollection.models.ErrorResponse
import aragones.sergio.readercollection.network.ApiManager
import aragones.sergio.readercollection.network.interfaces.BookApiService
import aragones.sergio.readercollection.database.AppDatabase
import aragones.sergio.readercollection.utils.Constants
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import javax.inject.Inject

class BooksRepository @Inject constructor(
    private val api: BookApiService,
    private val database: AppDatabase,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher
) : BaseRepository() {

    //region Private properties
    private val externalScope = CoroutineScope(Job() + mainDispatcher)
    //endregion

    //region Public methods
    fun loadBooks(success: () -> Unit, failure: (ErrorResponse) -> Unit) {
        success()
//        externalScope.launch {
//
//            try {
//                when (val response = ApiManager.validateResponse(api.getBooks())) {
//                    is RequestResult.JsonSuccess -> {
//
//                        val newBooks = response.body
//                        insertBooksDatabaseObserver(newBooks).subscribeBy(
//                            onComplete = {
//                                handleDisabledContentObserver(newBooks).subscribeBy(
//                                    onComplete = {
//                                        success()
//                                    },
//                                    onError = {
//                                        failure(
//                                            ErrorResponse(
//                                                Constants.EMPTY_VALUE,
//                                                R.string.error_database
//                                            )
//                                        )
//                                    }
//                                ).addTo(disposables)
//                            },
//                            onError = {
//                                failure(
//                                    ErrorResponse(
//                                        Constants.EMPTY_VALUE,
//                                        R.string.error_database
//                                    )
//                                )
//                            }
//                        ).addTo(disposables)
//                    }
//                    is RequestResult.Failure -> failure(response.error)
//                    else -> failure(ErrorResponse(Constants.EMPTY_VALUE, R.string.error_server))
//                }
//            } catch (e: Exception) {
//                failure(ErrorResponse(Constants.EMPTY_VALUE, R.string.error_server))
//            }
//        }
    }

    fun getBooksDatabaseObserver(
        format: String? = null,
        state: String? = null,
        isFavourite: Boolean? = null,
        sortParam: String? = null
    ): Maybe<List<BookResponse>> {

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
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }

    fun importDataFrom(jsonData: String): Completable {

        val listType = object : TypeToken<List<BookResponse?>?>() {}.type
        val books = Gson().fromJson<List<BookResponse?>>(jsonData, listType).mapNotNull { it }
        return database
            .bookDao()
            .insertBooksObserver(books)
            .`as`(RxJavaBridge.toV3Completable())
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }

    fun exportDataTo(): Single<String> {

        return Single.create<String> { emitter ->
            getBooksDatabaseObserver(null, null, null, null).subscribeBy(
                onComplete = {
                    emitter.onSuccess("")
                },
                onSuccess = {
                    emitter.onSuccess(Gson().toJson(it))
                },
                onError = {
                    emitter.onError(it)
                }
            ).addTo(disposables)
        }
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }

    fun getBookDatabaseObserver(googleId: String): Single<BookResponse> {

        return database
            .bookDao()
            .getBookObserver(googleId)
            .`as`(RxJavaBridge.toV3Single())
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }

    fun createBook(newBook: BookResponse, success: () -> Unit, failure: (ErrorResponse) -> Unit) {
        insertBooksDatabaseObserver(listOf(newBook)).subscribeBy(
            onComplete = {
                success()
            },
            onError = {
                failure(
                    ErrorResponse(
                        Constants.EMPTY_VALUE,
                        R.string.error_database
                    )
                )
            }
        ).addTo(disposables)
//        externalScope.launch {
//
//            try {
//                when (val response = ApiManager.validateResponse(api.createBook(newBook))) {
//                    is RequestResult.Success -> loadBooks(success, failure)
//                    is RequestResult.Failure -> failure(response.error)
//                    else -> failure(ErrorResponse(Constants.EMPTY_VALUE, R.string.error_server))
//                }
//            } catch (e: Exception) {
//                failure(ErrorResponse(Constants.EMPTY_VALUE, R.string.error_server))
//            }
//        }
    }

    fun setBook(
        book: BookResponse,
        success: (BookResponse) -> Unit,
        failure: (ErrorResponse) -> Unit
    ) {
        updateBooksDatabaseObserver(listOf(book)).subscribeBy(
            onComplete = {
                success(book)
            },
            onError = {
                failure(
                    ErrorResponse(
                        Constants.EMPTY_VALUE,
                        R.string.error_database
                    )
                )
            }
        ).addTo(disposables)
//        externalScope.launch {
//
//            try {
//                when (val response = ApiManager.validateResponse(api.setBook(book.id, book))) {
//                    is RequestResult.JsonSuccess -> {
//                        updateBooksDatabaseObserver(listOf(book)).subscribeBy(
//                            onComplete = {
//                                success(book)
//                            },
//                            onError = {
//                                failure(
//                                    ErrorResponse(
//                                        Constants.EMPTY_VALUE,
//                                        R.string.error_database
//                                    )
//                                )
//                            }
//                        ).addTo(disposables)
//                    }
//                    is RequestResult.Failure -> failure(response.error)
//                    else -> failure(ErrorResponse(Constants.EMPTY_VALUE, R.string.error_server))
//                }
//            } catch (e: Exception) {
//                failure(ErrorResponse(Constants.EMPTY_VALUE, R.string.error_server))
//            }
//        }
    }

    fun deleteBook(bookId: String, success: () -> Unit, failure: (ErrorResponse) -> Unit) {
        getBookDatabaseObserver(bookId).subscribeBy(
            onSuccess = { book ->
                deleteBooksDatabaseObserver(listOf(book)).subscribeBy(
                    onComplete = {
                        success()
                    },
                    onError = {
                        failure(
                            ErrorResponse(
                                Constants.EMPTY_VALUE,
                                R.string.error_database
                            )
                        )
                    }
                ).addTo(disposables)
            },
            onError = {
                failure(
                    ErrorResponse(
                        Constants.EMPTY_VALUE,
                        R.string.error_database
                    )
                )
            }
        ).addTo(disposables)
//        externalScope.launch {
//
//            try {
//                when (val response = ApiManager.validateResponse(api.deleteBook(bookId))) {
//                    is RequestResult.Success -> {
//                        getBookDatabaseObserver(bookId).subscribeBy(
//                            onSuccess = { book ->
//                                deleteBooksDatabaseObserver(listOf(book)).subscribeBy(
//                                    onComplete = {
//                                        success()
//                                    },
//                                    onError = {
//                                        failure(
//                                            ErrorResponse(
//                                                Constants.EMPTY_VALUE,
//                                                R.string.error_database
//                                            )
//                                        )
//                                    }
//                                ).addTo(disposables)
//                            },
//                            onError = {
//                                failure(
//                                    ErrorResponse(
//                                        Constants.EMPTY_VALUE,
//                                        R.string.error_database
//                                    )
//                                )
//                            }
//                        ).addTo(disposables)
//                    }
//                    is RequestResult.Failure -> failure(response.error)
//                    else -> failure(ErrorResponse(Constants.EMPTY_VALUE, R.string.error_server))
//                }
//            } catch (e: Exception) {
//                failure(ErrorResponse(Constants.EMPTY_VALUE, R.string.error_server))
//            }
//        }
    }

    fun setFavouriteBook(
        bookId: String,
        isFavourite: Boolean,
        success: (BookResponse) -> Unit,
        failure: (ErrorResponse) -> Unit
    ) {
        getBookDatabaseObserver(bookId).subscribeBy(
            onSuccess = { book ->
                book.isFavourite = isFavourite
                updateBooksDatabaseObserver(listOf(book)).subscribeBy(
                    onComplete = {
                        success(book)
                    },
                    onError = {
                        failure(
                            ErrorResponse(
                                Constants.EMPTY_VALUE,
                                R.string.error_database
                            )
                        )
                    }
                ).addTo(disposables)
            },
            onError = {
                failure(
                    ErrorResponse(
                        Constants.EMPTY_VALUE,
                        R.string.error_database
                    )
                )
            }
        ).addTo(disposables)
//        externalScope.launch {
//
//            try {
//                when (val response = ApiManager.validateResponse(
//                    api.setFavouriteBook(
//                        bookId,
//                        FavouriteBook(isFavourite)
//                    )
//                )) {
//                    is RequestResult.JsonSuccess -> {
//
//                        val book = response.body
//                        updateBooksDatabaseObserver(listOf(book)).subscribeBy(
//                            onComplete = {
//                                success(book)
//                            },
//                            onError = {
//                                failure(
//                                    ErrorResponse(
//                                        Constants.EMPTY_VALUE,
//                                        R.string.error_database
//                                    )
//                                )
//                            }
//                        ).addTo(disposables)
//                    }
//                    is RequestResult.Failure -> failure(response.error)
//                    else -> failure(ErrorResponse(Constants.EMPTY_VALUE, R.string.error_server))
//                }
//            } catch (e: Exception) {
//                failure(ErrorResponse(Constants.EMPTY_VALUE, R.string.error_server))
//            }
//        }
    }

    fun resetTableObserver(): Completable {
        return Completable.create { emitter ->

            getBooksDatabaseObserver().subscribeBy(
                onComplete = {
                    emitter.onComplete()
                },
                onSuccess = { books ->
                    deleteBooksDatabaseObserver(books).subscribeBy(
                        onComplete = {
                            emitter.onComplete()
                        },
                        onError = {
                            emitter.onError(it)
                        }
                    ).addTo(disposables)
                },
                onError = {
                    emitter.onError(it)
                }
            ).addTo(disposables)
        }.subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER).observeOn(ApiManager.OBSERVER_SCHEDULER)
    }
    //endregion

    //region Private methods
    private fun insertBooksDatabaseObserver(books: List<BookResponse>): Completable {
        return database
            .bookDao()
            .insertBooksObserver(books)
            .`as`(RxJavaBridge.toV3Completable())
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }

    private fun updateBooksDatabaseObserver(books: List<BookResponse>): Completable {
        return database
            .bookDao()
            .updateBooksObserver(books)
            .`as`(RxJavaBridge.toV3Completable())
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }

    private fun deleteBooksDatabaseObserver(books: List<BookResponse>): Completable {
        return database
            .bookDao()
            .deleteBooksObserver(books)
            .`as`(RxJavaBridge.toV3Completable())
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }
    //endregion
}