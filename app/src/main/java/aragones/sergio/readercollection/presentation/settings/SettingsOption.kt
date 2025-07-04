/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 2/7/2025
 */

package aragones.sergio.readercollection.presentation.settings

sealed class SettingsOption {
    object Account : SettingsOption()
    object DataSync : SettingsOption()
    object DisplaySettings : SettingsOption()
    object Logout : SettingsOption()
}