/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.network.apiservice

import aragones.sergio.readercollection.models.GoogleBookListResponse
import aragones.sergio.readercollection.models.GoogleBookResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.http.*

interface GooglePIService {

    @GET("volumes")
    fun searchGoogleBooks(@QueryMap queryParams: Map<String, String>): Single<GoogleBookListResponse>

    @GET("volumes/{volumeId}")
    fun getGoogleBook(@Path(value = "volumeId") volumeId: String): Single<GoogleBookResponse>
}