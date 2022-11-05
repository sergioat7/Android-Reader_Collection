/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import aragones.sergio.readercollection.base.BaseActivity
import aragones.sergio.readercollection.models.FormatResponse
import aragones.sergio.readercollection.models.StateResponse
import aragones.sergio.readercollection.utils.Constants
import aragones.sergio.readercollection.viewmodelfactories.LandingViewModelFactory
import aragones.sergio.readercollection.viewmodels.LandingViewModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.gson.Gson
import org.json.JSONObject
import java.util.*

class LandingActivity : BaseActivity() {

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
        viewModel = ViewModelProvider(
            this,
            LandingViewModelFactory(application)
        )[LandingViewModel::class.java]
        setupBindings()

        configLanguage()
        fetchRemoteConfigValues()
        viewModel.checkTheme()
        viewModel.checkIsLoggedIn()
    }

    private fun setupBindings() {

        viewModel.landingClassToStart.observe(this) {

            val intent = Intent(this, it)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun configLanguage() {

        val language = viewModel.language
        val conf = resources.configuration
        conf.setLocale(Locale(language))
        resources.updateConfiguration(conf, resources.displayMetrics)
    }

    private fun fetchRemoteConfigValues() {

        val remoteConfig = Firebase.remoteConfig.apply {
            setConfigSettingsAsync(
                remoteConfigSettings {
                    minimumFetchIntervalInSeconds = 3600
                }
            )
        }

        setupFormats(remoteConfig.getString("formats"))
        setupStates(remoteConfig.getString("states"))

        remoteConfig.fetchAndActivate().addOnCompleteListener(this) {

            setupFormats(remoteConfig.getString("formats"))
            setupStates(remoteConfig.getString("states"))
        }
    }

    private fun setupFormats(formatsString: String) {

        if (formatsString.isNotEmpty()) {
            var formats = listOf<FormatResponse>()
            try {
                val languagedFormats =
                    JSONObject(formatsString).get(viewModel.language).toString()
                formats =
                    Gson().fromJson(languagedFormats, Array<FormatResponse>::class.java).asList()
            } catch (e: Exception) {
                Log.e("LandingActivity", e.message ?: "")
            }

            Constants.FORMATS = formats
        }
    }

    private fun setupStates(statesString: String) {

        if (statesString.isNotEmpty()) {
            var states = listOf<StateResponse>()
            try {
                val languagedStates =
                    JSONObject(statesString).get(viewModel.language).toString()
                states =
                    Gson().fromJson(languagedStates, Array<StateResponse>::class.java).asList()
            } catch (e: Exception) {
                Log.e("LandingActivity", e.message ?: "")
            }

            Constants.STATES = states
        }
    }
    //endregion
}