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

    //MARK: - Private properties

    private lateinit var viewModel: LandingViewModel

    //MARK: - Lifecycle methods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeUI()
    }

    //MARK: - Private methods

    private fun initializeUI() {
        viewModel = ViewModelProvider(this, LandingViewModelFactory(application)).get(LandingViewModel::class.java)
        setupBindings()

        configLanguage()
        viewModel.checkVersion()
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
}