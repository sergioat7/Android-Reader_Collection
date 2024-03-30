/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 29/3/2024
 */

package aragones.sergio.readercollection.domain

import aragones.sergio.readercollection.data.remote.ApiManager
import aragones.sergio.readercollection.data.remote.services.BookApiService
import aragones.sergio.readercollection.data.remote.services.GoogleApiService
import aragones.sergio.readercollection.domain.di.MainDispatcher
import aragones.sergio.readercollection.data.remote.model.BookResponse
import aragones.sergio.readercollection.data.remote.model.ErrorResponse
import aragones.sergio.readercollection.data.remote.model.GoogleBookListResponse
import com.aragones.sergio.data.business.GoogleBookResponse
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import javax.inject.Inject

class BooksRemoteDataSource @Inject constructor(
    private val booksApiService: BookApiService,
    private val googleApiService: GoogleApiService,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher
) {

    //region Private properties
    private val externalScope = CoroutineScope(Job() + mainDispatcher)
    //endregion

    //region Public methods
    fun loadBooks(success: () -> Unit, failure: (ErrorResponse) -> Unit) {
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

    fun createBook(newBook: BookResponse, success: () -> Unit, failure: (ErrorResponse) -> Unit) {
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
//        externalScope.launch {
//
//            try {
//                when (val response = ApiManager.validateResponse(api.setBook(book.id, book))) {
//                    is RequestResult.JsonSuccess -> success(book)
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
//        externalScope.launch {
//
//            try {
//                when (val response = ApiManager.validateResponse(
//                    api.setFavouriteBook(
//                        bookId,
//                        FavouriteBook(isFavourite)
//                    )
//                )) {
//                    is RequestResult.JsonSuccess -> success(response.body)
//                    is RequestResult.Failure -> failure(response.error)
//                    else -> failure(ErrorResponse(Constants.EMPTY_VALUE, R.string.error_server))
//                }
//            } catch (e: Exception) {
//                failure(ErrorResponse(Constants.EMPTY_VALUE, R.string.error_server))
//            }
//        }
    }

    fun deleteBook(bookId: String, success: () -> Unit, failure: (ErrorResponse) -> Unit) {
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

    fun searchBooksObserver(
        query: String,
        page: Int,
        order: String?
    ): Single<GoogleBookListResponse> {

        val params: MutableMap<String, String> = HashMap()
        params[ApiManager.SEARCH_PARAM] = query
        params[ApiManager.PAGE_PARAM] = ((page - 1) * ApiManager.RESULTS).toString()
        params[ApiManager.RESULTS_PARAM] = ApiManager.RESULTS.toString()
        if (order != null) {
            params[ApiManager.ORDER_PARAM] = order
        }
        return googleApiService
            .searchGoogleBooks(params)
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }

    fun getBookObserver(volumeId: String): Single<GoogleBookResponse> {

        return googleApiService
            .getGoogleBook(volumeId)
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }
    //endregion
}