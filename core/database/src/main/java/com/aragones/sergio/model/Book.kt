/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 29/3/2024
 */

package com.aragones.sergio.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "Book")
data class Book(
    @PrimaryKey
    override val id: String,
    val title: String?,
    val subtitle: String?,
    val authors: List<String>?,
    val publisher: String?,
    val publishedDate: Date?,
    val readingDate: Date?,
    val description: String?,
    val summary: String?,
    val isbn: String?,
    val pageCount: Int,
    val categories: List<String>?,
    val averageRating: Double,
    val ratingsCount: Int,
    val rating: Double,
    val thumbnail: String?,
    val image: String?,
    val format: String?,
    var state: String?,
    var isFavourite: Boolean,
    var priority: Int
) : BaseEntity<String>