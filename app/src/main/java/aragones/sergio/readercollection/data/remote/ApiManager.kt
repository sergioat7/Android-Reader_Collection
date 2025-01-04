/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 29/3/2024
 */

package aragones.sergio.readercollection.data.remote

import android.util.Log
import aragones.sergio.readercollection.BuildConfig
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.remote.model.ErrorResponse
import aragones.sergio.readercollection.data.remote.model.RequestResult
import com.aragones.sergio.util.Constants
import com.squareup.moshi.Moshi
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

object ApiManager {

    //region Static properties
    const val ACCEPT_LANGUAGE_HEADER = "Accept-Language"
    const val AUTHORIZATION_HEADER = "Authorization"
    //endregion

    //region Public properties
    val moshi: Moshi = Moshi.Builder().add(MoshiDateAdapter(Constants.DATE_FORMAT)).build()
    var retrofits: MutableMap<KClass<*>, Any> = mutableMapOf()
    var apis: MutableMap<KClass<*>, Any> = mutableMapOf()
    var language: String = ""
    var accessToken: String = ""
    //endregion

    //region Public methods
    inline fun <reified T : Any> getRetrofit(url: String): Retrofit =
        retrofits[T::class] as Retrofit? ?: run {
            val logInterceptor = HttpLoggingInterceptor()
            logInterceptor.level =
                if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.HEADERS
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }

            val clientBuilder =
                OkHttpClient
                    .Builder()
                    .addInterceptor(logInterceptor)
                    .addInterceptor(TokenInterceptor())
                    .connectTimeout(2, TimeUnit.MINUTES)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .followRedirects(false)

            val retrofit =
                Retrofit
                    .Builder()
                    .baseUrl(url)
                    .client(clientBuilder.build())
                    .addConverterFactory(MoshiConverterFactory.create(moshi))
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .build()

            retrofits[T::class] = retrofit
            retrofit
        }

    inline fun <reified T : Any> getService(url: String): T = apis[T::class] as? T ?: run {
        val ret = getRetrofit<T>(url).create(T::class.java)
        apis[T::class] = ret
        ret
    }

    inline fun <reified T : Any> validateResponse(
        response: retrofit2.Response<T>,
    ): RequestResult<T> {
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
                        ErrorResponse(Constants.EMPTY_VALUE, R.string.error_server),
                    )
                }
            }
            code == 302 -> {
                RequestResult.Failure(
                    ErrorResponse(Constants.EMPTY_VALUE, R.string.error_resource_found),
                )
            }
            code < 500 && error != null -> {
                RequestResult.Failure(
                    moshi.adapter(ErrorResponse::class.java).fromJson(error.charStream().toString())
                        ?: ErrorResponse(Constants.EMPTY_VALUE, R.string.error_server),
                )
            }
            else -> {
                RequestResult.Failure(
                    ErrorResponse(Constants.EMPTY_VALUE, R.string.error_server),
                )
            }
        }
    }

    inline fun <reified T : Any> getEmptyResponse(): RequestResult<T>? {
        try {
            val result = moshi.adapter(T::class.java).fromJson("{}")!!
            return RequestResult.JsonSuccess(result)
        } catch (e: Exception) {
            Log.e("ApiManager", e.printStackTrace().toString())
        }

        try {
            val result = moshi.adapter(T::class.java).fromJson("[]")!!
            return RequestResult.JsonSuccess(result)
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
                        moshi
                            .adapter(ErrorResponse::class.java)
                            .fromJson(errorBody.charStream().toString())
                            ?: ErrorResponse(Constants.EMPTY_VALUE, R.string.error_server)
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
                original
                    .newBuilder()
                    .addHeader(ACCEPT_LANGUAGE_HEADER, language)
                    .header(AUTHORIZATION_HEADER, accessToken)
                    .build()
            } else {
                original
                    .newBuilder()
                    .addHeader(ACCEPT_LANGUAGE_HEADER, language)
                    .build()
            }

            return chain.proceed(request)
        }
    }
    //endregion
}