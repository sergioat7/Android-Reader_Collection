/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 29/3/2024
 */

package aragones.sergio.readercollection.data.remote

import com.aragones.sergio.data.business.ErrorResponse

sealed class RequestResult<out T> {
    data object Success : RequestResult<Nothing>()
    data class JsonSuccess<out T>(val body: T) : RequestResult<T>()
    data class Failure(val error: ErrorResponse) : RequestResult<Nothing>()
}