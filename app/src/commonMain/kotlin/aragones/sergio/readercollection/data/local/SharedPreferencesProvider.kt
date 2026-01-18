/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 5/1/2026
 */

package aragones.sergio.readercollection.data.local

interface SharedPreferencesProvider {
    fun writeBoolean(key: String, value: Boolean, isEncrypted: Boolean = false)
    fun writeInt(key: String, value: Int, isEncrypted: Boolean = false)
    fun writeString(key: String, value: String?, isEncrypted: Boolean = false)
    fun readBoolean(key: String, defaultValue: Boolean, isEncrypted: Boolean = false): Boolean
    fun readInt(key: String, defaultValue: Int, isEncrypted: Boolean = false): Int
    fun readString(key: String, isEncrypted: Boolean = false): String?
    fun removeValues(keys: List<String>, isEncrypted: Boolean = false)
}