/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.presentation.ui.landing

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.remote.ApiManager
import aragones.sergio.readercollection.presentation.ui.base.BaseActivity
import aragones.sergio.readercollection.utils.InAppUpdateService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.play.core.install.model.InstallStatus
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class LandingActivity : BaseActivity() {

    //region Private properties
    private val viewModel: LandingViewModel by viewModels()
    private val inAppUpdateService by lazy { InAppUpdateService(this) }
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
        viewModel.fetchRemoteConfigValues()
        viewModel.checkTheme()

        inAppUpdateService.checkVersion()
        inAppUpdateService.installStatus.observe(this) {
            when (it) {
                InstallStatus.DOWNLOADING,
                InstallStatus.DOWNLOADED,
                InstallStatus.INSTALLED,
                InstallStatus.CANCELED,
                -> {
                    launchApp()
                    inAppUpdateService.onDestroy()
                }
                InstallStatus.FAILED -> {
                    inAppUpdateService.checkVersion()
                }
                else -> {
                    Unit
                }
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
        ApiManager.language = viewModel.language
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            val language = viewModel.language
            val conf = resources.configuration
            conf.setLocale(Locale(language))
            resources.updateConfiguration(conf, resources.displayMetrics)
        } else {
            val locale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
            viewModel.setLanguage(locale.language)
        }
    }

    private fun showPopupActionDialog(message: String, acceptHandler: () -> Unit) {
        MaterialAlertDialogBuilder(this)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(resources.getString(R.string.accept)) { dialog, _ ->
                acceptHandler()
                dialog.dismiss()
            }.show()
    }
    //endregion
}