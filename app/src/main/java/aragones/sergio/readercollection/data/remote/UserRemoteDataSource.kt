/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 30/3/2024
 */

package aragones.sergio.readercollection.data.remote

import aragones.sergio.readercollection.data.remote.model.ErrorResponse
import javax.inject.Inject

class UserRemoteDataSource @Inject constructor() {

    //region Public methods
    fun login(
        username: String,
        password: String,
        success: (String) -> Unit,
        failure: (ErrorResponse) -> Unit,
    ) {
        success("-")
    }

    fun logout() {}

    fun register(
        username: String,
        password: String,
        success: () -> Unit,
        failure: (ErrorResponse) -> Unit,
    ) {
        success()
    }

    fun updatePassword(password: String, success: () -> Unit, failure: (ErrorResponse) -> Unit) {
        success()
    }

    fun deleteUser(success: () -> Unit, failure: (ErrorResponse) -> Unit) {
        success()
    }
    //endregion
}