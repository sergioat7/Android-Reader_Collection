/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/1/2026
 */

package aragones.sergio.readercollection.data.local

import platform.Foundation.NSBundle
import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
import platform.Foundation.preferredLanguages

actual class AppInfoProvider {

    actual fun getVersion(): String? =
        NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String

    actual fun getCurrentLanguage(): String =
        (NSLocale.preferredLanguages.first() as String).split("-").first()

    actual fun changeLocale(language: String) {
        NSUserDefaults.standardUserDefaults.setObject(arrayListOf(language), "AppleLanguages")
    }

    actual fun applyTheme(themeMode: Int) {}
}