/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/10/2020
 */

package aragones.sergio.readercollection.models.responses

import aragones.sergio.readercollection.models.base.BaseModel
import com.google.gson.annotations.SerializedName

data class StateResponse(
    @SerializedName("id")
    override val id: String,
    @SerializedName("name")
    val name: String
): BaseModel<String>