/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/10/2020
 */

package aragones.sergio.readercollection.models

import com.google.gson.annotations.SerializedName

data class GoogleIsbnResponse(
    @SerializedName("type")
    val type: String?,
    @SerializedName("identifier")
    val identifier: String?
)