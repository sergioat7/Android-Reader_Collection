/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.data.remote.services

import aragones.sergio.readercollection.data.remote.ApiManager
import aragones.sergio.readercollection.data.remote.model.BookResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface BookApiService {

    @Headers(
        "Accept:application/json",
        "${ApiManager.AUTHORIZATION_HEADER}:_",
    )
    @GET("books")
    suspend fun getBooks(): Response<List<BookResponse>>

    @Headers(
        "Content-Type:application/json",
        "${ApiManager.AUTHORIZATION_HEADER}:_",
    )
    @POST("book")
    suspend fun createBook(@Body body: BookResponse): Response<Unit>

    @Headers(
        "Content-Type:application/json",
        "${ApiManager.AUTHORIZATION_HEADER}:_",
    )
    @PATCH("book/{googleId}")
    suspend fun setBook(
        @Path(value = "googleId") googleId: String,
        @Body body: BookResponse,
    ): Response<BookResponse>

    @Headers(
        "${ApiManager.AUTHORIZATION_HEADER}:_",
    )
    @DELETE("book/{googleId}")
    suspend fun deleteBook(@Path(value = "googleId") googleId: String): Response<Unit>
}