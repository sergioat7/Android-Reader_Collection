/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 17/10/2020
 */

package aragones.sergio.readercollection.network

import aragones.sergio.readercollection.models.requests.LoginCredentials
import aragones.sergio.readercollection.models.requests.NewPassword
import aragones.sergio.readercollection.models.responses.LoginResponse
import retrofit2.Response
import retrofit2.http.*

interface UserApiService {

    @Headers(
        "Content-Type:application/json"
    )
    @POST("user/session")
    suspend fun login(@Body body: LoginCredentials): Response<LoginResponse>

    @Headers(
        "${ApiManager.AUTHORIZATION_HEADER}:_"
    )
    @DELETE("user/session")
    suspend fun logout(): Response<Unit>

    @Headers(
        "Content-Type:application/json"
    )
    @POST("user")
    suspend fun register(@Body body: LoginCredentials): Response<Unit>

    @Headers(
        "${ApiManager.AUTHORIZATION_HEADER}:_"
    )
    @DELETE("user")
    suspend fun deleteUser(): Response<Unit>

    @Headers(
        "Content-Type:application/json",
        "${ApiManager.AUTHORIZATION_HEADER}:_"
    )
    @PUT("user/updatePassword")
    suspend fun updatePassword(@Body body: NewPassword): Response<Unit>
}