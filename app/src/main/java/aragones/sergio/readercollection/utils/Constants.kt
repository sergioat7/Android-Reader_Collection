/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/10/2020
 */

package aragones.sergio.readercollection.utils

import aragones.sergio.readercollection.data.remote.model.FormatResponse
import aragones.sergio.readercollection.data.remote.model.StateResponse

object Constants {

    var FORMATS = listOf(
        FormatResponse("DIGITAL", "Digital"),
        FormatResponse("PHYSICAL", "Physical"),
    )
    var STATES = listOf(
        StateResponse("PENDING", "Pending"),
        StateResponse("READ", "Read"),
        StateResponse("READING", "Reading"),
    )
}
