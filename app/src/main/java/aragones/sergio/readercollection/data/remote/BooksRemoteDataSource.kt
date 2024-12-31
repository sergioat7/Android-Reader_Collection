/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 29/3/2024
 */

package aragones.sergio.readercollection.data.remote

import android.util.Log
import aragones.sergio.readercollection.BuildConfig
import aragones.sergio.readercollection.data.remote.model.FormatResponse
import aragones.sergio.readercollection.data.remote.model.GoogleBookListResponse
import aragones.sergio.readercollection.data.remote.model.GoogleBookResponse
import aragones.sergio.readercollection.data.remote.model.StateResponse
import aragones.sergio.readercollection.data.remote.services.GoogleApiService
import aragones.sergio.readercollection.utils.Constants
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.squareup.moshi.Moshi
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject
import org.json.JSONObject

class BooksRemoteDataSource @Inject constructor(
    private val googleApiService: GoogleApiService,
    private val remoteConfig: FirebaseRemoteConfig,
) {

    //region Private properties
    private val SEARCH_PARAM = "q"
    private val PAGE_PARAM = "startIndex"
    private val RESULTS_PARAM = "maxResults"
    private val ORDER_PARAM = "orderBy"
    private val API_KEY = "key"
    private val RESULTS = 20
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