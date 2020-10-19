/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 17/10/2020
 */

package aragones.sergio.readercollection.network.apiservice

import aragones.sergio.readercollection.models.requests.LoginCredentials
import aragones.sergio.readercollection.models.responses.LoginResponse
import aragones.sergio.readercollection.models.requests.NewPassword
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import retrofit2.http.*

interface UserAPIService {

    @Headers(
        "Content-Type:application/json"
    )
    @POST("user")
    fun register(@HeaderMap headers: Map<String, String>, @Body body: LoginCredentials): Completable

    @DELETE("user")
    fun deleteUser(@HeaderMap headers: Map<String, String>): Completable

    @Headers(
        "Content-Type:application/json"
    )
    @POST("user/session")
    fun login(@HeaderMap headers: Map<String, String>, @Body body: LoginCredentials): Single<LoginResponse>

    @DELETE("user/session")
    fun logout(@HeaderMap headers: Map<String, String>): Completable

    @Headers(
        "Content-Type:application/json"
    )
    @PUT("user/updatePassword")
    fun updatePassword(@HeaderMap headers: Map<String, String>, @Body body: NewPassword): Completable
}