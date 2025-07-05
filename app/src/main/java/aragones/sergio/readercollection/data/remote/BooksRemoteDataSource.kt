/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 29/3/2024
 */

package aragones.sergio.readercollection.data.remote

import android.util.Log
import aragones.sergio.readercollection.BuildConfig
import aragones.sergio.readercollection.data.remote.model.BookResponse
import aragones.sergio.readercollection.data.remote.model.FormatResponse
import aragones.sergio.readercollection.data.remote.model.GoogleBookListResponse
import aragones.sergio.readercollection.data.remote.model.GoogleBookResponse
import aragones.sergio.readercollection.data.remote.model.StateResponse
import aragones.sergio.readercollection.data.remote.services.GoogleApiService
import aragones.sergio.readercollection.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.squareup.moshi.Moshi
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject
import org.json.JSONObject

class BooksRemoteDataSource @Inject constructor(
    private val googleApiService: GoogleApiService,
    private val remoteConfig: FirebaseRemoteConfig,
    private val firestore: FirebaseFirestore,
) {

    //region Static properties
    companion object {
        private const val SEARCH_PARAM = "q"
        private const val PAGE_PARAM = "startIndex"
        private const val RESULTS_PARAM = "maxResults"
        private const val ORDER_PARAM = "orderBy"
        private const val API_KEY = "key"
        private const val RESULTS = 20
    }
    //endregion

    //region Private properties
    private val moshi = Moshi.Builder().build()
    //endregion

    //region Public methods
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

    fun getBooks(uuid: String): Single<List<BookResponse>> = Single.create { emitter ->
        firestore
            .collection("users")
            .document(uuid)
            .collection("books")
            .get()
            .addOnSuccessListener { result ->
                val books = result.toObjects(BookResponse::class.java)
                emitter.onSuccess(books)
            }.addOnFailureListener { emitter.onError(it) }
    }

    fun syncBooks(
        uuid: String,
        booksToSave: List<BookResponse>,
        booksToRemove: List<BookResponse>,
    ) = Completable.create { emitter ->
        val batch = firestore.batch()
        val booksRef = firestore
            .collection("users")
            .document(uuid)
            .collection("books")

        booksToSave.forEach { book ->
            val docRef = booksRef.document(book.id)
            batch.set(docRef, book)
        }

        booksToRemove.forEach { book ->
            val docRef = booksRef.document(book.id)
            batch.delete(docRef)
        }

        batch
            .commit()
            .addOnSuccessListener {
                emitter.onComplete()
            }.addOnFailureListener {
                emitter.onError(it)
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