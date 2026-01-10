/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 15/10/2020
 */

package aragones.sergio.readercollection.presentation

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.navigation.Navigator
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

class MainActivity : ComponentActivity(), AndroidScopeComponent {

    //region Public properties
    override val scope: Scope by activityScope()
    //endregion

    //region Private properties
    private val inAppUpdateService: InAppUpdateService by inject()
    private val navigator: Navigator by inject()
    private val viewModel: MainViewModel by viewModel()
    private var appUpdated = mutableStateOf(false)
    //endregion

    //region Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ReaderCollectionApp {
                CompositionLocalProvider(LocalLanguage provides viewModel.language) {
                    val scope = rememberCoroutineScope()
                    val snackbarHostState = remember { SnackbarHostState() }
                    MainScreen(navigator, snackbarHostState)

                    val message = stringResource(R.string.message_app_update_downloaded)
                    val action = stringResource(R.string.restart)
                    LaunchedEffect(appUpdated.value) {
                        if (appUpdated.value) {
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = message,
                                    actionLabel = action,
                                    duration = SnackbarDuration.Indefinite,
                                )
                                when (result) {
                                    SnackbarResult.ActionPerformed -> {
                                        inAppUpdateService.completeUpdate()
                                    }
                                    SnackbarResult.Dismissed -> { /*no-op*/ }
                                }
                            }
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            inAppUpdateService.installStatus.collect {
                if (it == InstallStatus.DOWNLOADED) {
                    inAppUpdateService.onResume()
                } else if (it == InstallStatus.DOWNLOADED + InstallStatus.INSTALLED) {
                    appUpdated.value = true
                }
            }
        }

        onBackPressedDispatcher.addCallback {
            moveTaskToBack(true)
        }
    }

    override fun onResume() {
        super.onResume()

        inAppUpdateService.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val locale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
            viewModel.setLanguage(locale.language)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        inAppUpdateService.onDestroy()
    }
    //endregion
}

val LocalLanguage: ProvidableCompositionLocal<String> = staticCompositionLocalOf {
    error("CompositionLocal LocalLanguage not present")
}