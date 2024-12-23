/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 17/10/2020
 */

package aragones.sergio.readercollection.data.remote.services

import aragones.sergio.readercollection.data.remote.ApiManager
import aragones.sergio.readercollection.data.remote.model.LoginCredentials
import aragones.sergio.readercollection.data.remote.model.LoginResponse
import aragones.sergio.readercollection.data.remote.model.NewPassword
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT

interface UserApiService {

    @Headers(
        "Content-Type:application/json",
    )
    @POST("user/session")
    suspend fun login(@Body body: LoginCredentials): Response<LoginResponse>

    @Headers(
        "${ApiManager.AUTHORIZATION_HEADER}:_",
    )
    @DELETE("user/session")
    suspend fun logout(): Response<Unit>

    @Headers(
        "Content-Type:application/json",
    )
    @POST("user")
    suspend fun register(@Body body: LoginCredentials): Response<Unit>

    @Headers(
        "${ApiManager.AUTHORIZATION_HEADER}:_",
    )
    @DELETE("user")
    suspend fun deleteUser(): Response<Unit>

    @Headers(
        "Content-Type:application/json",
        "${ApiManager.AUTHORIZATION_HEADER}:_",
    )
    @PUT("user/updatePassword")
    suspend fun updatePassword(@Body body: NewPassword): Response<Unit>
}