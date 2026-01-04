/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 23/2/2024
 */

package aragones.sergio.readercollection.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class FormatResponse(
    override val id: String,
    val name: String,
) : BaseModel<String>