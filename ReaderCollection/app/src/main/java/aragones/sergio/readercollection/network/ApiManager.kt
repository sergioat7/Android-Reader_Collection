/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 2/1/2022
 */

package aragones.sergio.readercollection.network

import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.extensions.toDate
import aragones.sergio.readercollection.models.responses.ErrorResponse
import aragones.sergio.readercollection.utils.Constants
import com.google.gson.*
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.util.*
import java.util.concurrent.TimeUnit

object ApiManager {

    //region Static properties
    const val BASE_ENDPOINT = "https://books-collection-services.herokuapp.com/"
    const val BASE_GOOGLE_ENDPOINT = "https://www.googleapis.com/books/v1/"
    const val ACCEPT_LANGUAGE_HEADER = "Accept-Language"
    const val AUTHORIZATION_HEADER = "Authorization"
    const val CONNECT_TIMEOUT: Long = 60
    const val READ_TIMEOUT: Long = 30
    const val WRITE_TIMEOUT: Long = 15
    const val SEARCH_PARAM = "q"
    const val PAGE_PARAM = "startIndex"
    const val RESULTS_PARAM = "maxResults"
    const val ORDER_PARAM = "orderBy"
    const val RESULTS = 20
    val SUBSCRIBER_SCHEDULER: Scheduler = Schedulers.io()
    val OBSERVER_SCHEDULER: Scheduler = AndroidSchedulers.mainThread()
    //endregion

    //region Public properties
    val gson: Gson =
        GsonBuilder()
            .registerTypeAdapter(
                Date::class.java,
                JsonDeserializer<Date> { json: JsonElement, _: Type?, _: JsonDeserializationContext? ->
                    json.asString.toDate()
                }
            )
            .setDateFormat(Constants.DATE_FORMAT)
            .serializeNulls()
            .create()

    private val okHttpClient: OkHttpClient =
        OkHttpClient
            .Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .followRedirects(false)
            .build()

    val retrofit: Retrofit =
        Retrofit
            .Builder()
            .baseUrl(BASE_ENDPOINT)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()

    val googleRetrofit: Retrofit =
        Retrofit
            .Builder()
            .baseUrl(BASE_GOOGLE_ENDPOINT)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()
    //endregion

    //region Public methods
    fun handleError(error: Throwable): ErrorResponse {

        lateinit var errorResponse: ErrorResponse
        if (error is HttpException) {

            if (error.code() == 302) {
                errorResponse = ErrorResponse("", R.string.error_resource_found)
            } else {
                error.response()?.errorBody()?.let { errorBody ->

                    errorResponse = try {
                        gson.fromJson(
                            errorBody.charStream(), ErrorResponse::class.java
                        )
                    } catch (e: Exception) {
                        ErrorResponse("", R.string.error_server)
                    }
                } ?: run {
                    errorResponse = ErrorResponse("", R.string.error_server)
                }
            }
        } else {
            errorResponse = ErrorResponse("", R.string.error_server)
        }
        return errorResponse
    }
    //endregion
}