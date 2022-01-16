/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/1/2022
 */

package aragones.sergio.readercollection.models

import aragones.sergio.readercollection.base.BaseModel

data class StateResponse(
    override val id: String,
    val name: String
) : BaseModel<String>