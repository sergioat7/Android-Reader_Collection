/*
 * Copyright (c) 2023 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package aragones.sergio.readercollection.ui.landing

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.local.SharedPreferencesHandler
import aragones.sergio.readercollection.data.remote.model.FormatResponse
import aragones.sergio.readercollection.data.remote.model.StateResponse
import aragones.sergio.readercollection.ui.base.BaseActivity
import aragones.sergio.readercollection.utils.Constants
import aragones.sergio.readercollection.utils.InAppUpdateService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.play.core.install.model.InstallStatus
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.squareup.moshi.Moshi
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import java.util.*

@AndroidEntryPoint
class LandingActivity : BaseActivity() {

    //region Private properties
    private val viewModel: LandingViewModel by viewModels()
    private val inAppUpdateService by lazy { InAppUpdateService(this) }
    private val moshi = Moshi.Builder().build()
    //endregion

    //region Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeUI()
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.onDestroy()
        inAppUpdateService.onDestroy()
    }
    //endregion

    //region Private methods
    private fun initializeUI() {

        setupBindings()

        configLanguage()
        fetchRemoteConfigValues()
        viewModel.checkTheme()

        inAppUpdateService.checkVersion()
        inAppUpdateService.installStatus.observe(this) {
            when (it) {

                InstallStatus.DOWNLOADING, InstallStatus.DOWNLOADED, InstallStatus.INSTALLED, InstallStatus.CANCELED -> {
                    launchApp()
                    inAppUpdateService.onDestroy()
                }

                InstallStatus.FAILED -> inAppUpdateService.checkVersion()
                else -> Unit
            }
        }
    }

    private fun launchApp() {

        if (!viewModel.newChangesPopupShown) {
            showPopupActionDialog(getString(R.string.new_version_changes), acceptHandler = {
                viewModel.checkIsLoggedIn()
            })
        } else {
            viewModel.checkIsLoggedIn()
        }
    }

    private fun setupBindings() {

        viewModel.landingClassToStart.observe(this) {

            val intent = Intent(this, it)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun configLanguage() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {

            val language = viewModel.language
            val conf = resources.configuration
            conf.setLocale(Locale(language))
            resources.updateConfiguration(conf, resources.displayMetrics)
        } else {

            val locale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
            SharedPreferencesHandler.language = locale.language
        }
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
                formats = moshi.adapter(Array<FormatResponse>::class.java)
                    .fromJson(languagedFormats)?.asList() ?: listOf()
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
                states = moshi.adapter(Array<StateResponse>::class.java)
                    .fromJson(languagedStates)?.asList() ?: listOf()
            } catch (e: Exception) {
                Log.e("LandingActivity", e.message ?: "")
            }

            Constants.STATES = states
        }
    }

    private fun showPopupActionDialog(message: String, acceptHandler: () -> Unit) {

        MaterialAlertDialogBuilder(this)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(resources.getString(R.string.accept)) { dialog, _ ->
                acceptHandler()
                dialog.dismiss()
            }
            .show()
    }
    //endregion
}