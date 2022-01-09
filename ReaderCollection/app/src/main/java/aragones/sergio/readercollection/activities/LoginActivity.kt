/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/10/2020
 */

package aragones.sergio.readercollection.activities

import android.os.Bundle
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.base.BaseActivity

class LoginActivity : BaseActivity() {

    //region Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }
    //endregion
}