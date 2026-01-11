/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.presentation.landing

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import aragones.sergio.readercollection.presentation.navigation.Navigator
import aragones.sergio.readercollection.presentation.navigation.Route
import aragones.sergio.readercollection.presentation.navigation.authGraph
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionApp
import aragones.sergio.readercollection.utils.InAppUpdateService
import com.google.android.play.core.install.model.InstallStatus
import java.util.Locale
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.scope.Scope

class LandingActivity : ComponentActivity(), AndroidScopeComponent {

    //region Public properties
    override val scope: Scope by activityScope()
    //endregion

    //region Private properties
    private val inAppUpdateService: InAppUpdateService by inject()
    private val navigator: Navigator by inject()
    private val viewModel: LandingViewModel by viewModel()
    private var appUpdated = mutableStateOf(false)
    //endregion

    //region Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val skipAnimation = intent.getBooleanExtra("SKIP_ANIMATION", false)

        setUp()

        setContent {
            val animationFinished = rememberSaveable { mutableStateOf(false) }
            ReaderCollectionApp {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(WindowInsets.safeDrawing.asPaddingValues()),
                ) {
                    val navController = rememberNavController()
                    val isLogged = viewModel.isLogged.collectAsState()
                    when (isLogged.value) {
                        true -> {
                            navigator.goToMain()
                        }
                        false -> {
                            val navGraph = remember(navController) {
                                navController.createGraph(startDestination = Route.Auth) {
                                    authGraph(navController, navigator)
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
                            if (skipAnimation) {
                                animationFinished.value = true
                            } else {
                                LandingScreen(onAnimationFinished = {
                                    animationFinished.value = true
                                })
                            }
                        }
                    }
                }

                LaunchedEffect(appUpdated.value, animationFinished.value) {
                    if (appUpdated.value && animationFinished.value) {
                        viewModel.checkIsLoggedIn()
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
                        if (it == InstallStatus.CANCELED &&
                            inAppUpdateService.isImmediateUpdate()
                        ) {
                            finish()
                        } else {
                            appUpdated.value = true
                            inAppUpdateService.onDestroy()
                        }
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
    //endregion
}