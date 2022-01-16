/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/10/2020
 */

package aragones.sergio.readercollection.models.responses

import androidx.room.Entity
import androidx.room.PrimaryKey
import aragones.sergio.readercollection.base.BaseModel

@Entity(tableName = "Format")
data class FormatResponse(
    @PrimaryKey
    override val id: String,
    val name: String
) : BaseModel<String>