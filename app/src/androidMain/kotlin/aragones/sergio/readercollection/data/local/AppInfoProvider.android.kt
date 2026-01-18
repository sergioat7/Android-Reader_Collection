/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 5/1/2026
 */

package aragones.sergio.readercollection.data.local

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale

actual class AppInfoProvider(private val context: Context) {

    actual fun getVersion(): String? = context
        .packageManager
        .getPackageInfo(context.packageName, 0)
        .versionName

    actual fun getCurrentLanguage(): String = Locale.getDefault().language

    actual fun changeLocale(language: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val locale = context.getSystemService(LocaleManager::class.java)
            locale?.applicationLocales = LocaleList(Locale.forLanguageTag(language))
        }
    }

    actual fun applyTheme(themeMode: Int) {
        when (themeMode) {
            1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
            )
        }
    }
}