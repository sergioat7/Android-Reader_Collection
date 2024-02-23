/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 7/11/2022
 */

package aragones.sergio.readercollection.network

import com.aragones.sergio.data.ErrorResponse

sealed class RequestResult<out T> {
    data object Success : RequestResult<Nothing>()
    data class JsonSuccess<out T>(val body: T) : RequestResult<T>()
    data class Failure(val error: ErrorResponse) : RequestResult<Nothing>()
}