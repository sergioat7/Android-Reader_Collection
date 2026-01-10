/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 29/12/2025
 */

package aragones.sergio.readercollection.domain.model

import org.jetbrains.compose.resources.StringResource

data class ErrorModel(
    val error: String,
    val errorKey: StringResource,
)