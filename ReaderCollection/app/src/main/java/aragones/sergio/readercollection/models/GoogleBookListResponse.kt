/*
 * Copyright (c) 2023 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package aragones.sergio.readercollection.models

import com.google.gson.annotations.SerializedName

data class GoogleBookListResponse(
    @SerializedName("totalItems")
    val totalItems: Int,
    @SerializedName("items")
    var items: List<GoogleBookResponse>?
)