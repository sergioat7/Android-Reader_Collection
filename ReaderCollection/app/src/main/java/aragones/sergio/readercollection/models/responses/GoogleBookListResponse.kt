/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/10/2020
 */

package aragones.sergio.readercollection.models.responses

import com.google.gson.annotations.SerializedName

data class GoogleBookListResponse(
    @SerializedName("totalItems")
    val totalItems: Int,
    @SerializedName("items")
    var items: List<GoogleBookResponse>?
)