/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 23/2/2024
 */

package aragones.sergio.readercollection.data.remote.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FormatResponse(
    override val id: String,
    val name: String
) : BaseModel<String>