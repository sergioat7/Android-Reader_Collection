/*
 * Copyright (c) 2023 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package aragones.sergio.readercollection.network.interfaces

import aragones.sergio.readercollection.network.ApiManager
import com.aragones.sergio.data.auth.LoginCredentials
import com.aragones.sergio.data.auth.LoginResponse
import com.aragones.sergio.data.auth.NewPassword
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