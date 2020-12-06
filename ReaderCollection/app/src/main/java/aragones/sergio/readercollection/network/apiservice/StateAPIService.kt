/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 17/10/2020
 */

package aragones.sergio.readercollection.network.apiservice

import aragones.sergio.readercollection.models.responses.StateResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Headers

interface StateAPIService {

    @Headers(
        "Accept:application/json"
    )
    @GET("states")
    fun getStates(@HeaderMap headers: Map<String, String>): Single<List<StateResponse>>
}