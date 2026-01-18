/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/1/2026
 */

package aragones.sergio.readercollection.data.local

import platform.Foundation.NSUserDefaults

class UserDefaultsImpl : SharedPreferencesProvider {

    private val defaults = NSUserDefaults.standardUserDefaults

    override fun writeBoolean(key: String, value: Boolean, isEncrypted: Boolean) {
        TODO("Not yet implemented")
    }

    override fun writeInt(key: String, value: Int, isEncrypted: Boolean) {
        TODO("Not yet implemented")
    }

    override fun writeString(key: String, value: String?, isEncrypted: Boolean) {
        TODO("Not yet implemented")
    }

    override fun readBoolean(key: String, defaultValue: Boolean, isEncrypted: Boolean): Boolean {
        TODO("Not yet implemented")
    }

    override fun readInt(key: String, defaultValue: Int, isEncrypted: Boolean): Int {
        TODO("Not yet implemented")
    }

    override fun readString(key: String, isEncrypted: Boolean): String? {
        TODO("Not yet implemented")
    }

    override fun removeValues(keys: List<String>, isEncrypted: Boolean) {
        TODO("Not yet implemented")
    }
}