/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 29/3/2024
 */

package aragones.sergio.readercollection.data.remote

import aragones.sergio.readercollection.BuildConfig
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.remote.model.ErrorResponse
import com.aragones.sergio.util.Constants
import com.squareup.moshi.Moshi
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object ApiManager {

    //region Public properties
    val moshi: Moshi = Moshi.Builder().add(MoshiDateAdapter(Constants.DATE_FORMAT)).build()
    var retrofits: MutableMap<KClass<*>, Any> = mutableMapOf()
    var apis: MutableMap<KClass<*>, Any> = mutableMapOf()
    var language: String = ""
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
                    .build()

            retrofits[T::class] = retrofit
            retrofit
        }

    inline fun <reified T : Any> getService(url: String): T = apis[T::class] as? T ?: run {
        val ret = getRetrofit<T>(url).create(T::class.java)
        apis[T::class] = ret
        ret
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
                    } catch (_: Exception) {
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
}