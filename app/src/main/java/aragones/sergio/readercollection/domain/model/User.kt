/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 13/7/2025
 */

package aragones.sergio.readercollection.domain.model

import aragones.sergio.readercollection.data.remote.model.RequestStatus

data class User(
    val id: String,
    val username: String,
    val status: RequestStatus,
)
