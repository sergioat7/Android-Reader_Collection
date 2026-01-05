/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 5/1/2026
 */

package aragones.sergio.readercollection.data.local

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import java.util.Locale

class AppInfoProvider(private val context: Context) {

    fun getVersion(): String? = context
        .packageManager
        .getPackageInfo(context.packageName, 0)
        .versionName

    fun changeLocale(language: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val locale = context.getSystemService(LocaleManager::class.java)
            locale?.applicationLocales = LocaleList(Locale.forLanguageTag(language))
        }
    }
}