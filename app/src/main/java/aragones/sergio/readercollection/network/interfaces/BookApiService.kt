/*
 * Copyright (c) 2023 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package aragones.sergio.readercollection.network.interfaces

import aragones.sergio.readercollection.network.ApiManager
import com.aragones.sergio.data.business.BookResponse
import com.aragones.sergio.data.business.FavouriteBook
import retrofit2.Response
import retrofit2.http.*

interface BookApiService {

    @Headers(
        "Accept:application/json",
        "${ApiManager.AUTHORIZATION_HEADER}:_"
    )
    @GET("books")
    suspend fun getBooks(): Response<List<BookResponse>>

    @Headers(
        "Content-Type:application/json",
        "${ApiManager.AUTHORIZATION_HEADER}:_"
    )
    @POST("book")
    suspend fun createBook(@Body body: BookResponse): Response<Unit>

    @Headers(
        "Content-Type:application/json",
        "${ApiManager.AUTHORIZATION_HEADER}:_"
    )
    @PATCH("book/{googleId}")
    suspend fun setBook(
        @Path(value = "googleId") googleId: String,
        @Body body: BookResponse
    ): Response<BookResponse>

    @Headers(
        "${ApiManager.AUTHORIZATION_HEADER}:_"
    )
    @DELETE("book/{googleId}")
    suspend fun deleteBook(@Path(value = "googleId") googleId: String): Response<Unit>

    @Headers(
        "Content-Type:application/json",
        "${ApiManager.AUTHORIZATION_HEADER}:_"
    )
    @PATCH("book/{googleId}/favourite")
    suspend fun setFavouriteBook(
        @Path(value = "googleId") googleId: String,
        @Body body: FavouriteBook
    ): Response<BookResponse>
}