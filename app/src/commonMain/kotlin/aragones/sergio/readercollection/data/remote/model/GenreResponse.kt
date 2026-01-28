/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 28/1/2026
 */

package aragones.sergio.readercollection.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class GenreResponse(
    override val id: String,
    val name: String,
) : BaseModel<String>

var GENRES = listOf<GenreResponse>()
var ALL_GENRES: Map<String, List<GenreResponse>> = mapOf()