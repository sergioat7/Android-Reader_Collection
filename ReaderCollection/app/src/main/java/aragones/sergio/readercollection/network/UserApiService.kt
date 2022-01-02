/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 17/10/2020
 */

package aragones.sergio.readercollection.network

import aragones.sergio.readercollection.models.requests.LoginCredentials
import aragones.sergio.readercollection.models.responses.LoginResponse
import aragones.sergio.readercollection.models.requests.NewPassword
import aragones.sergio.readercollection.network.ApiManager
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import retrofit2.http.*

interface UserApiService {

    @Headers(
        "Content-Type:application/json"
    )
    @POST("user")
    fun register(@Body body: LoginCredentials): Completable

    @Headers(
        "${ApiManager.AUTHORIZATION_HEADER}:_"
    )
    @DELETE("user")
    fun deleteUser(): Completable

    @Headers(
        "Content-Type:application/json"
    )
    @POST("user/session")
    fun login(@Body body: LoginCredentials): Single<LoginResponse>

    @Headers(
        "${ApiManager.AUTHORIZATION_HEADER}:_"
    )
    @DELETE("user/session")
    fun logout(): Completable

    @Headers(
        "Content-Type:application/json",
        "${ApiManager.AUTHORIZATION_HEADER}:_"
    )
    @PUT("user/updatePassword")
    fun updatePassword(@Body body: NewPassword): Completable
}