/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.activities

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import aragones.sergio.readercollection.activities.base.BaseActivity
import aragones.sergio.readercollection.viewmodelfactories.LandingViewModelFactory
import aragones.sergio.readercollection.viewmodels.LandingViewModel
import java.util.*

class LandingActivity: BaseActivity() {

    //region Private properties
    private lateinit var viewModel: LandingViewModel
    //endregion

    //region Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onDestroy()
    }
    //endregion

    //region Private methods
    private fun initializeUI() {
        viewModel = ViewModelProvider(this, LandingViewModelFactory(application))[LandingViewModel::class.java]
        setupBindings()

        configLanguage()
        viewModel.checkVersion()
        viewModel.checkTheme()
    }

    private fun setupBindings() {

        viewModel.landingClassToStart.observe(this, {

            val intent = Intent(this, it)
            startActivity(intent)
        })
    }

    private fun configLanguage() {

        val language = viewModel.language
        val conf = resources.configuration
        conf.setLocale(Locale(language))
        resources.updateConfiguration(conf, resources.displayMetrics)
    }
    //endregion
}