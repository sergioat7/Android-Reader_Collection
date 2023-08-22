/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/1/2022
 */

package aragones.sergio.readercollection.models

import aragones.sergio.readercollection.models.base.BaseModel

data class FormatResponse(
    override val id: String,
    val name: String
) : BaseModel<String>