/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 5/1/2026
 */

package aragones.sergio.readercollection.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.aragones.sergio.util.Preferences

class SharedPreferencesProviderImpl(context: Context) : SharedPreferencesProvider {

    private val appPreferences = context.getSharedPreferences(
        Preferences.PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    )
    private val editor = appPreferences.edit()
    private val appEncryptedPreferences = EncryptedSharedPreferences.create(
        Preferences.ENCRYPTED_PREFERENCES_NAME,
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )
    private val encryptedEditor = appEncryptedPreferences.edit()

    override fun writeBoolean(key: String, value: Boolean, isEncrypted: Boolean) {
        if (isEncrypted) {
            encryptedEditor
        } else {
            editor
        }.apply {
            putBoolean(key, value)
            commit()
        }
    }

    override fun writeInt(key: String, value: Int, isEncrypted: Boolean) {
        if (isEncrypted) {
            encryptedEditor
        } else {
            editor
        }.apply {
            putInt(key, value)
            commit()
        }
    }

    override fun writeString(key: String, value: String?, isEncrypted: Boolean) {
        if (isEncrypted) {
            encryptedEditor
        } else {
            editor
        }.apply {
            putString(key, value)
            commit()
        }
    }

    override fun readBoolean(key: String, defaultValue: Boolean, isEncrypted: Boolean): Boolean =
        if (isEncrypted) {
            appEncryptedPreferences
        } else {
            appPreferences
        }.getBoolean(key, defaultValue)

    override fun readInt(key: String, defaultValue: Int, isEncrypted: Boolean): Int =
        if (isEncrypted) {
            appEncryptedPreferences
        } else {
            appPreferences
        }.getInt(key, defaultValue)

    override fun readString(key: String, isEncrypted: Boolean): String? = if (isEncrypted) {
        appEncryptedPreferences
    } else {
        appPreferences
    }.getString(key, null)

    override fun removeValues(keys: List<String>, isEncrypted: Boolean) {
        if (isEncrypted) {
            encryptedEditor
        } else {
            editor
        }.apply {
            for (key in keys) {
                remove(key)
            }
        }.apply()
    }
}