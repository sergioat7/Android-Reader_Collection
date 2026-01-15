/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 15/1/2026
 */

package aragones.sergio.readercollection.presentation

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.res.stringResource
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.navigation.Navigator
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionApp
import kotlinx.coroutines.launch

@Composable
fun MainView(
    navigator: Navigator,
    viewModel: MainViewModel,
    isAppUpdated: Boolean = false,
    onCompleteUpdate: () -> Unit = {},
) {
    ReaderCollectionApp {
        CompositionLocalProvider(LocalLanguage provides viewModel.language) {
            val scope = rememberCoroutineScope()
            val snackbarHostState = remember { SnackbarHostState() }
            MainScreen(navigator, snackbarHostState)

            val message = stringResource(R.string.message_app_update_downloaded)
            val action = stringResource(R.string.restart)
            LaunchedEffect(isAppUpdated) {
                if (isAppUpdated) {
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = message,
                            actionLabel = action,
                            duration = SnackbarDuration.Indefinite,
                        )
                        when (result) {
                            SnackbarResult.ActionPerformed -> {
                                onCompleteUpdate()
                            }
                            SnackbarResult.Dismissed -> { /*no-op*/ }
                        }
                    }
                }
            }
        }
    }
}

val LocalLanguage: ProvidableCompositionLocal<String> = staticCompositionLocalOf {
    error("CompositionLocal LocalLanguage not present")
}