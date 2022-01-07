/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/10/2020
 */

package aragones.sergio.readercollection.activities

import android.os.Bundle
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.activities.base.BaseActivity
import aragones.sergio.readercollection.fragments.LoginFragment

class LoginActivity: BaseActivity() {

    //region Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, LoginFragment.newInstance())
                .commitNow()
        }
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }
    //endregion
}