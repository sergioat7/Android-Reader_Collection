/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 29/3/2024
 */

package aragones.sergio.readercollection.data.remote

import android.util.Log
import aragones.sergio.readercollection.BuildConfig
import aragones.sergio.readercollection.data.remote.di.MainDispatcher
import aragones.sergio.readercollection.data.remote.model.BookResponse
import aragones.sergio.readercollection.data.remote.model.ErrorResponse
import aragones.sergio.readercollection.data.remote.model.FormatResponse
import aragones.sergio.readercollection.data.remote.model.GoogleBookListResponse
import aragones.sergio.readercollection.data.remote.model.GoogleBookResponse
import aragones.sergio.readercollection.data.remote.model.StateResponse
import aragones.sergio.readercollection.data.remote.services.BookApiService
import aragones.sergio.readercollection.data.remote.services.GoogleApiService
import aragones.sergio.readercollection.utils.Constants
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.squareup.moshi.Moshi
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.json.JSONObject

class BooksRemoteDataSource @Inject constructor(
    private val booksApiService: BookApiService,
    private val googleApiService: GoogleApiService,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher,
    private val remoteConfig: FirebaseRemoteConfig,
) {

    //region Private properties
    private val SEARCH_PARAM = "q"
    private val PAGE_PARAM = "startIndex"
    private val RESULTS_PARAM = "maxResults"
    private val ORDER_PARAM = "orderBy"
    private val API_KEY = "key"
    private val RESULTS = 20
    private val externalScope = CoroutineScope(Job() + mainDispatcher)
    private val moshi = Moshi.Builder().build()
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
//                        insertBooks(newBooks).subscribeBy(
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
        failure: (ErrorResponse) -> Unit,
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
        failure: (ErrorResponse) -> Unit,
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
//                        getBook(bookId).subscribeBy(
//                            onSuccess = { book ->
//                                deleteBooks(listOf(book)).subscribeBy(
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

    fun searchBooks(query: String, page: Int, order: String?): Single<GoogleBookListResponse> {
        val params = mutableMapOf(
            API_KEY to BuildConfig.API_KEY,
            SEARCH_PARAM to query,
            PAGE_PARAM to ((page - 1) * RESULTS).toString(),
            RESULTS_PARAM to RESULTS.toString(),
        )
        if (order != null) {
            params[ORDER_PARAM] = order
        }
        return googleApiService.searchGoogleBooks(params)
    }

    fun getBook(volumeId: String): Single<GoogleBookResponse> {
        val params = mapOf(API_KEY to BuildConfig.API_KEY)
        return googleApiService.getGoogleBook(volumeId, params)
    }

    fun fetchRemoteConfigValues(language: String) {
        setupFormats(remoteConfig.getString("formats"), language)
        setupStates(remoteConfig.getString("states"), language)

        remoteConfig.fetchAndActivate().addOnCompleteListener {
            setupFormats(remoteConfig.getString("formats"), language)
            setupStates(remoteConfig.getString("states"), language)
        }
    }
    //endregion

    //region Private methods
    private fun setupFormats(formatsString: String, language: String) {
        if (formatsString.isNotEmpty()) {
            var formats = listOf<FormatResponse>()
            try {
                val languagedFormats =
                    JSONObject(formatsString).get(language).toString()
                formats = moshi
                    .adapter(Array<FormatResponse>::class.java)
                    .fromJson(languagedFormats)
                    ?.asList() ?: listOf()
            } catch (e: Exception) {
                Log.e("BooksRemoteDataSource", e.message ?: "")
            }
            Constants.FORMATS = formats
        }
    }

    private fun setupStates(statesString: String, language: String) {
        if (statesString.isNotEmpty()) {
            var states = listOf<StateResponse>()
            try {
                val languagedStates =
                    JSONObject(statesString).get(language).toString()
                states = moshi
                    .adapter(Array<StateResponse>::class.java)
                    .fromJson(languagedStates)
                    ?.asList() ?: listOf()
            } catch (e: Exception) {
                Log.e("BooksRemoteDataSource", e.message ?: "")
            }
            Constants.STATES = states
        }
    }
    //endregion
}