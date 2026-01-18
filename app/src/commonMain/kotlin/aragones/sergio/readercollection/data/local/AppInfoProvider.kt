/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 6/1/2026
 */

package aragones.sergio.readercollection.data.local

expect class AppInfoProvider {
    fun getVersion(): String?
    fun getCurrentLanguage(): String
    fun changeLocale(language: String)
    fun applyTheme(themeMode: Int)
}