/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/10/2020
 */

package aragones.sergio.readercollection.network.apiclient

import aragones.sergio.readercollection.utils.Constants
import com.google.gson.*
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.util.*
import java.util.concurrent.TimeUnit

class APIClient {
    companion object {

        val gson: Gson =
            GsonBuilder()
                .registerTypeAdapter(
                    Date::class.java,
                    JsonDeserializer<Date> { json: JsonElement, _: Type?, _: JsonDeserializationContext? ->
                        Constants.stringToDate(json.asString)
                    }
                )
                .setDateFormat(Constants.DATE_FORMAT)
                .serializeNulls()
                .create()

        private val okHttpClient: OkHttpClient =
            OkHttpClient
                .Builder()
                .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.SECONDS)
                .build()

        val retrofit: Retrofit =
            Retrofit
                .Builder()
                .baseUrl(Constants.BASE_ENDPOINT)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build()

        val googleRetrofit: Retrofit =
            Retrofit
                .Builder()
                .baseUrl(Constants.BASE_GOOGLE_ENDPOINT)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build()
    }
}