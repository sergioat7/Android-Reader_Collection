/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/1/2026
 */

package aragones.sergio.readercollection.data.local

import platform.Foundation.NSUserDefaults

class UserDefaultsImpl : SharedPreferencesProvider {

    private val defaults = NSUserDefaults.standardUserDefaults

    override fun writeBoolean(key: String, value: Boolean, isEncrypted: Boolean) {
        defaults.setBool(value, key)
    }

    override fun writeInt(key: String, value: Int, isEncrypted: Boolean) {
        defaults.setInteger(value.toLong(), key)
    }

    override fun writeString(key: String, value: String?, isEncrypted: Boolean) {
        defaults.setObject(value, key)
    }

    override fun readBoolean(key: String, defaultValue: Boolean, isEncrypted: Boolean): Boolean =
        defaults.boolForKey(key)

    override fun readInt(key: String, defaultValue: Int, isEncrypted: Boolean): Int =
        defaults.integerForKey(key).toInt()

    override fun readString(key: String, isEncrypted: Boolean): String? = defaults.stringForKey(key)

    override fun removeValues(keys: List<String>, isEncrypted: Boolean) {
        for (key in keys) {
            defaults.removeObjectForKey(key)
        }
    }
}