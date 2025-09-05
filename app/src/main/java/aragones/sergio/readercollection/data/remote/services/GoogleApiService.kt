/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.data.remote.services

import aragones.sergio.readercollection.data.remote.model.GoogleBookListResponse
import aragones.sergio.readercollection.data.remote.model.GoogleBookResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface GoogleApiService {

    @GET("volumes")
    suspend fun searchGoogleBooks(
        @QueryMap queryParams: Map<String, String>,
    ): GoogleBookListResponse

    @GET("volumes/{volumeId}")
    suspend fun getGoogleBook(
        @Path(value = "volumeId") volumeId: String,
        @QueryMap queryParams: Map<String, String>,
    ): GoogleBookResponse
}