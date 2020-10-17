/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.models

import aragones.sergio.readercollection.models.base.GoogleVolumeResponse
import com.google.gson.annotations.SerializedName

data class GoogleBookResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("volumeInfo")
    val volumeInfo: GoogleVolumeResponse
)