/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.activities

import android.content.Intent
import android.os.Bundle
import aragones.sergio.readercollection.activities.base.BaseActivity
import aragones.sergio.readercollection.utils.SharedPreferencesHandler
import java.util.*

class LandingActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPrefHandler = SharedPreferencesHandler(this)

        val language: String = sharedPrefHandler.getLanguage()
        val conf = resources.configuration
        conf.setLocale(Locale(language))
        resources.updateConfiguration(conf, resources.displayMetrics)

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}