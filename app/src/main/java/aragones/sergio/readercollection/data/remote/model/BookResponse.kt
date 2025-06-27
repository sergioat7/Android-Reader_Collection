/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 23/2/2024
 */

package aragones.sergio.readercollection.data.remote.model

import java.util.Date

data class BookResponse(
    override val id: String = "",
    val title: String? = null,
    val subtitle: String? = null,
    val authors: List<String>? = null,
    val publisher: String? = null,
    val publishedDate: Date? = null,
    val readingDate: Date? = null,
    val description: String? = null,
    val summary: String? = null,
    val isbn: String? = null,
    val pageCount: Int = 0,
    val categories: List<String>? = null,
    val averageRating: Double = 0.0,
    val ratingsCount: Int = 0,
    val rating: Double = 0.0,
    val thumbnail: String? = null,
    val image: String? = null,
    val format: String? = null,
    var state: String? = null,
    var priority: Int = -1,
) : BaseModel<String>