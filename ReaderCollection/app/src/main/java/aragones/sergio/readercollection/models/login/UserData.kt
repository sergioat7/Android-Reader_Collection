/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/10/2020
 */

package aragones.sergio.readercollection.models.login

data class UserData(
    var username: String,
    var password: String,
    var isLoggedIn: Boolean
)