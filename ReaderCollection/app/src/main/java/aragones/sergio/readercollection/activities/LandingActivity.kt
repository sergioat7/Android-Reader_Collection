/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.activities

import android.content.Intent
import android.os.Bundle
import aragones.sergio.readercollection.activities.base.BaseActivity
import aragones.sergio.readercollection.injection.ReaderCollectionApplication
import aragones.sergio.readercollection.utils.SharedPreferencesHandler
import java.util.*
import javax.inject.Inject
import kotlin.math.max

class LandingActivity: BaseActivity() {

    //MARK: - Public properties

    @Inject
    lateinit var sharedPrefHandler: SharedPreferencesHandler

    //MARK: - Lifecycle methods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as ReaderCollectionApplication).sharedPreferencesComponent.inject(this)

        val language: String = sharedPrefHandler.getLanguage()
        val conf = resources.configuration
        conf.setLocale(Locale(language))
        resources.updateConfiguration(conf, resources.displayMetrics)

        val initTime = System.currentTimeMillis() / 1000
        val cls: Class<*> = if (sharedPrefHandler.isLoggedIn()) {
            MainActivity::class.java
        } else {
            LoginActivity::class.java
        }
        val finalTime = System.currentTimeMillis() / 1000
        val taskTime = finalTime - initTime
        val time = max(0, 1000 - taskTime)

        try {
            Thread.sleep(time)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val intent = Intent(this, cls)
        startActivity(intent)
    }
}