/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.network

import aragones.sergio.readercollection.models.requests.FavouriteBook
import aragones.sergio.readercollection.models.responses.BookResponse
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import retrofit2.http.*

interface BookApiService {

    @Headers(
        "Accept:application/json",
        "${ApiManager.AUTHORIZATION_HEADER}:_"
    )
    @GET("books")
    fun getBooks(): Maybe<List<BookResponse>>

    @Headers(
        "Content-Type:application/json",
        "${ApiManager.AUTHORIZATION_HEADER}:_"
    )
    @POST("book")
    fun createBook(@Body body: BookResponse): Completable

    @Headers(
        "Content-Type:application/json",
        "${ApiManager.AUTHORIZATION_HEADER}:_"
    )
    @PATCH("book/{googleId}")
    fun setBook(
        @Path(value = "googleId") googleId: String,
        @Body body: BookResponse
    ): Single<BookResponse>

    @Headers(
        "${ApiManager.AUTHORIZATION_HEADER}:_"
    )
    @DELETE("book/{googleId}")
    fun deleteBook(@Path(value = "googleId") googleId: String): Completable

    @Headers(
        "Content-Type:application/json",
        "${ApiManager.AUTHORIZATION_HEADER}:_"
    )
    @PATCH("book/{googleId}/favourite")
    fun setFavouriteBook(
        @Path(value = "googleId") googleId: String,
        @Body body: FavouriteBook
    ): Single<BookResponse>
}