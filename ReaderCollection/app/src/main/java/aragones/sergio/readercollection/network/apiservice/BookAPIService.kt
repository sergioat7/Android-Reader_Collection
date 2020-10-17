/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.network.apiservice

import aragones.sergio.readercollection.models.BookResponse
import aragones.sergio.readercollection.models.FavouriteBook
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import retrofit2.http.*

interface BookAPIService {

    @Headers(
        "Accept:application/json"
    )
    @GET("books")
    fun getBooks(@HeaderMap headers: Map<String, String>, @QueryMap queryParams: Map<String, String>): Single<List<BookResponse>>

    @Headers(
        "Accept:application/json"
    )
    @GET("book/{googleId}")
    fun getBook(@HeaderMap headers: Map<String, String>, @Path(value = "googleId") googleId: String): Single<BookResponse>

    @Headers(
        "Content-Type:application/json"
    )
    @POST("book")
    fun createBook(@HeaderMap headers: Map<String, String>, @Body body: BookResponse): Completable

    @Headers(
        "Content-Type:application/json"
    )
    @PATCH("book/{googleId}")
    fun setBook(@HeaderMap headers: Map<String, String>, @Path(value = "googleId") googleId: String, @Body body: BookResponse): Single<BookResponse>

    @DELETE("book/{googleId}")
    fun deleteBook(@HeaderMap headers: Map<String, String>, @Path(value = "googleId") googleId: String): Completable

    @Headers(
        "Content-Type:application/json"
    )
    @PATCH("book/{googleId}/favourite")
    fun setFavouriteBook(@HeaderMap headers: Map<String, String>, @Path(value = "googleId") googleId: String, @Body body: FavouriteBook): Single<BookResponse>
}