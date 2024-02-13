/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/2/2022
 */

package aragones.sergio.readercollection.extensions

import android.content.SharedPreferences

fun SharedPreferences.Editor.setString(key: String, value: String?) {
    putString(key, value)
    commit()
}

fun SharedPreferences.Editor.setInt(key: String, value: Int) {
    putInt(key, value)
    commit()
}

fun SharedPreferences.Editor.setBoolean(key: String, value: Boolean) {
    putBoolean(key, value)
    commit()
}