/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 15/10/2020
 */

package aragones.sergio.readercollection.presentation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import aragones.sergio.readercollection.presentation.ui.theme.ReaderCollectionTheme
import aragones.sergio.readercollection.utils.InAppUpdateService
import com.google.android.play.core.install.model.InstallStatus
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    //region Private properties
    private lateinit var inAppUpdateService: InAppUpdateService
    //endregion

    //region Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ReaderCollectionScreen {
            }
        }

        inAppUpdateService = InAppUpdateService(this)
        inAppUpdateService.installStatus.observe(this) {
            if (it == InstallStatus.DOWNLOADED) {
                inAppUpdateService.onResume()
            }
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

@Composable
private fun ReaderCollectionScreen(content: @Composable () -> Unit) {
    ReaderCollectionTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.safeDrawing.asPaddingValues()),
            content = content,
        )
    }
}