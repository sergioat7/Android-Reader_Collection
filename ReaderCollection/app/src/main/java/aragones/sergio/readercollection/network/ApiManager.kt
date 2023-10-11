/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 2/1/2022
 */

package aragones.sergio.readercollection.network

import android.util.Log
import aragones.sergio.readercollection.BuildConfig
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.extensions.toDate
import aragones.sergio.readercollection.models.ErrorResponse
import aragones.sergio.readercollection.utils.Constants
import aragones.sergio.readercollection.data.source.SharedPreferencesHandler
import com.google.gson.*
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

object ApiManager {

    //region Static properties
    const val BASE_ENDPOINT = "https://books-collection-services.herokuapp.com/"
    const val BASE_GOOGLE_ENDPOINT = "https://www.googleapis.com/books/v1/"
    const val ACCEPT_LANGUAGE_HEADER = "Accept-Language"
    const val AUTHORIZATION_HEADER = "Authorization"
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

    var retrofits: MutableMap<KClass<*>, Any> = mutableMapOf()

    var apis: MutableMap<KClass<*>, Any> = mutableMapOf()
    //endregion

    //region Public methods
    inline fun <reified T : Any> getRetrofit(url: String): Retrofit {
        return retrofits[T::class] as Retrofit? ?: run {

            val logInterceptor = HttpLoggingInterceptor()
            logInterceptor.level =
                if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.HEADERS
                else HttpLoggingInterceptor.Level.NONE

            val clientBuilder =
                OkHttpClient.Builder()
                    .addInterceptor(logInterceptor)
                    .addInterceptor(TokenInterceptor())
                    .connectTimeout(2, TimeUnit.MINUTES)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .followRedirects(false)

            val retrofit =
                Retrofit.Builder()
                    .baseUrl(url)
                    .client(clientBuilder.build())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .build()

            retrofits[T::class] = retrofit
            retrofit
        }
    }

    inline fun <reified T : Any> getService(url: String): T {
        return apis[T::class] as? T ?: run {

            val ret = getRetrofit<T>(url).create(T::class.java)
            apis[T::class] = ret
            ret
        }
    }

    inline fun <reified T : Any> validateResponse(response: retrofit2.Response<T>): RequestResult<T> {

        val isSuccessful = response.isSuccessful
        val code = response.code()
        val body = response.body()
        val error = response.errorBody()
        return when {
            isSuccessful -> {
                when {
                    T::class == Unit::class -> RequestResult.Success
                    body != null && code != 204 -> RequestResult.JsonSuccess(body)
                    else -> getEmptyResponse() ?: RequestResult.Failure(
                        ErrorResponse(Constants.EMPTY_VALUE, R.string.error_server)
                    )
                }
            }
            code == 302 -> {
                RequestResult.Failure(
                    ErrorResponse(Constants.EMPTY_VALUE, R.string.error_resource_found)
                )
            }
            code < 500 && error != null -> {
                RequestResult.Failure(
                    gson.fromJson(
                        error.charStream(),
                        ErrorResponse::class.java
                    )
                )
            }
            else -> {
                RequestResult.Failure(
                    ErrorResponse(Constants.EMPTY_VALUE, R.string.error_server)
                )
            }
        }
    }

    inline fun <reified T : Any> getEmptyResponse(): RequestResult<T>? {

        try {
            return RequestResult.JsonSuccess(gson.fromJson("{}", T::class.java))
        } catch (e: Exception) {
            Log.e("ApiManager", e.printStackTrace().toString())
        }

        try {
            return RequestResult.JsonSuccess(gson.fromJson("[]", T::class.java))
        } catch (e: Exception) {
            Log.e("ApiManager", e.printStackTrace().toString())
        }

        return null
    }

    fun handleError(error: Throwable): ErrorResponse {

        lateinit var errorResponse: ErrorResponse
        if (error is HttpException) {

            if (error.code() == 302) {
                errorResponse = ErrorResponse(Constants.EMPTY_VALUE, R.string.error_resource_found)
            } else {
                error.response()?.errorBody()?.let { errorBody ->

                    errorResponse = try {
                        gson.fromJson(
                            errorBody.charStream(), ErrorResponse::class.java
                        )
                    } catch (e: Exception) {
                        ErrorResponse(Constants.EMPTY_VALUE, R.string.error_server)
                    }
                } ?: run {
                    errorResponse = ErrorResponse(Constants.EMPTY_VALUE, R.string.error_server)
                }
            }
        } else {
            errorResponse = ErrorResponse(Constants.EMPTY_VALUE, R.string.error_server)
        }
        return errorResponse
    }
    //endregion

    //region TokenInterceptor
    class TokenInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {

            val authRequirement = chain.request().header(AUTHORIZATION_HEADER)
            val original = chain.request()

            val request = if (authRequirement != null) {

                val accessToken = SharedPreferencesHandler.credentials.token
                original.newBuilder()
                    .addHeader(ACCEPT_LANGUAGE_HEADER, SharedPreferencesHandler.language)
                    .header(AUTHORIZATION_HEADER, accessToken)
                    .build()
            } else {
                original.newBuilder()
                    .addHeader(ACCEPT_LANGUAGE_HEADER, SharedPreferencesHandler.language)
                    .build()
            }

            return chain.proceed(request)
        }
    }
    //endregion
}