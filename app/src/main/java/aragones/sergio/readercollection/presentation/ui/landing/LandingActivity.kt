/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.presentation.ui.landing

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.remote.ApiManager
import aragones.sergio.readercollection.presentation.ui.MainActivity
import aragones.sergio.readercollection.presentation.ui.components.InformationAlertDialog
import aragones.sergio.readercollection.presentation.ui.login.LoginActivity
import aragones.sergio.readercollection.presentation.ui.navigation.Route
import aragones.sergio.readercollection.presentation.ui.navigation.authGraph
import aragones.sergio.readercollection.presentation.ui.theme.ReaderCollectionApp
import aragones.sergio.readercollection.utils.InAppUpdateService
import com.google.android.play.core.install.model.InstallStatus
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LandingActivity : ComponentActivity() {

    //region Private properties
    private val viewModel: LandingViewModel by viewModels()
    private val inAppUpdateService by lazy { InAppUpdateService(this) }
    private var appUpdated = false
    //endregion

    //region Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setUp()

        setContent {
            val showDialog = rememberSaveable { mutableStateOf(false) }
            val animationFinished = rememberSaveable { mutableStateOf(false) }
            ReaderCollectionApp {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(WindowInsets.safeDrawing.asPaddingValues()),
                ) {
                    val navController = rememberNavController()
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                    ) { padding ->
                        Box(modifier = Modifier.padding(padding)) {
                            val cls = viewModel.landingClassToStart.collectAsState()
                            when (cls.value) {
                                MainActivity::class.java -> {
                                    launchMainActivity()
                                }
                                LoginActivity::class.java -> {
                                    val navGraph = remember(navController) {
                                        navController.createGraph(startDestination = Route.Auth) {
                                            authGraph(navController)
                                        }
                                    }
                                    NavHost(
                                        navController = navController,
                                        graph = navGraph,
                                        enterTransition = { EnterTransition.None },
                                        exitTransition = { ExitTransition.None },
                                    )
                                }
                                else -> {
                                    LandingScreen(onAnimationFinished = {
                                        animationFinished.value = true
                                    })
                                }
                            }
                        }
                    }
                }

                if (showDialog.value) {
                    InformationAlertDialog(
                        show = true,
                        text = stringResource(R.string.new_version_changes),
                    ) {
                        viewModel.checkIsLoggedIn()
                        showDialog.value = false
                    }
                }

                LaunchedEffect(appUpdated, animationFinished.value) {
                    if (appUpdated && animationFinished.value) {
                        if (!viewModel.newChangesPopupShown) {
                            showDialog.value = true
                        } else {
                            viewModel.checkIsLoggedIn()
                        }
                    }
                }
            }
        }

        onBackPressedDispatcher.addCallback {
            moveTaskToBack(true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        inAppUpdateService.onDestroy()
    }
    //endregion

    //region Private methods
    private fun setUp() {
        configLanguage()
        viewModel.fetchRemoteConfigValues()
        viewModel.checkTheme()

        inAppUpdateService.checkVersion()
        lifecycleScope.launch {
            inAppUpdateService.installStatus.collect {
                when (it) {
                    InstallStatus.DOWNLOADING,
                    InstallStatus.DOWNLOADED,
                    InstallStatus.INSTALLED,
                    InstallStatus.CANCELED,
                    -> {
                        appUpdated = true
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

    private fun launchMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }
    //endregion
}