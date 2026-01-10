/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 12/1/2026
 */

package aragones.sergio.readercollection.presentation.theme

import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import aragones.sergio.readercollection.utils.SyncDataWorker
import java.util.concurrent.TimeUnit

actual object AppUiProvider {

    @Composable
    actual fun isDarkThemeApplied(): Boolean {
        val darkTheme = when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_YES -> true
            AppCompatDelegate.MODE_NIGHT_NO -> false
            else -> isSystemInDarkTheme()
        }
        return darkTheme
    }

    @Composable
    actual fun applyBarsStyle(
        isDarkTheme: Boolean,
        colors: ColorScheme,
        statusBarSameAsBackground: Boolean,
        navigationBarSameAsBackground: Boolean,
    ) {
        val systemBarAsBackground = if (isDarkTheme) {
            SystemBarStyle.dark(
                colors.secondary.toArgb(),
            )
        } else {
            SystemBarStyle.light(
                colors.secondary.toArgb(),
                colors.secondary.toArgb(),
            )
        }
        val systemBarOppositeToBackground = if (isDarkTheme) {
            SystemBarStyle.light(
                colors.primary.toArgb(),
                colors.primary.toArgb(),
            )
        } else {
            SystemBarStyle.dark(colors.primary.toArgb())
        }

        val statusBarStyle = if (statusBarSameAsBackground) {
            systemBarAsBackground
        } else {
            systemBarOppositeToBackground
        }
        val navigationBarStyle = if (navigationBarSameAsBackground) {
            systemBarAsBackground
        } else {
            systemBarOppositeToBackground
        }

        val activity = LocalActivity.current as ComponentActivity
        SideEffect {
            activity.enableEdgeToEdge(
                statusBarStyle = statusBarStyle,
                navigationBarStyle = navigationBarStyle,
            )
        }
    }

    @Composable
    actual fun isPortrait(): Boolean =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

    @Composable
    actual fun getScreenWidth(): Int = LocalConfiguration.current.screenWidthDp

    @Composable
    actual fun launchWorker() {
        val context = LocalContext.current
        val workRequest = PeriodicWorkRequestBuilder<SyncDataWorker>(7, TimeUnit.DAYS)
            .setConstraints(
                Constraints
                    .Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            ).build()
        WorkManager
            .getInstance(context)
            .enqueueUniquePeriodicWork(
                uniqueWorkName = SyncDataWorker.WORK_NAME,
                existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.KEEP,
                request = workRequest,
            )
    }

    @Composable
    actual fun cancelWorker() {
        val context = LocalContext.current
        WorkManager
            .getInstance(context)
            .cancelUniqueWork(SyncDataWorker.WORK_NAME)
    }
}