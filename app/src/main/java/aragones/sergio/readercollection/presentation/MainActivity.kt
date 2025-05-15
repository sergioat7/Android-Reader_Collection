/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 15/10/2020
 */

package aragones.sergio.readercollection.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.lifecycleScope
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionApp
import aragones.sergio.readercollection.utils.InAppUpdateService
import com.google.android.play.core.install.model.InstallStatus
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    //region Public properties
    @Inject
    lateinit var inAppUpdateService: InAppUpdateService
    //endregion

    //region Private properties
    private val viewModel: MainViewModel by viewModels()
    //endregion

    //region Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ReaderCollectionApp {
                CompositionLocalProvider(LocalLanguage provides viewModel.language) {
                    MainScreen()
                }
            }
        }

        lifecycleScope.launch {
            inAppUpdateService.installStatus.collect {
                if (it == InstallStatus.DOWNLOADED) {
                    inAppUpdateService.onResume()
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