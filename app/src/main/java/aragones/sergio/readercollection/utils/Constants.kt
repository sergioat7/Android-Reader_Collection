/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/10/2020
 */

package aragones.sergio.readercollection.utils

import androidx.fragment.app.FragmentActivity
import aragones.sergio.readercollection.R
import com.aragones.sergio.data.FormatResponse
import com.aragones.sergio.data.StateResponse
import com.getkeepsafe.taptargetview.TapTarget
import com.google.android.material.bottomnavigation.BottomNavigationView

object Constants {

    var FORMATS = listOf(
        FormatResponse("DIGITAL", "Digital"),
        FormatResponse("PHYSICAL", "Physical")
    )
    var STATES = listOf(
        StateResponse("PENDING", "Pending"),
        StateResponse("READ", "Read"),
        StateResponse("READING", "Reading")
    )

    fun createTargetForBottomNavigationView(
        activity: FragmentActivity?,
        id: Int,
        title: String,
        description: String?
    ): TapTarget {
        return TapTarget.forView(
            activity?.findViewById<BottomNavigationView>(R.id.nav_view)?.findViewById(id),
            title,
            description
        )
    }
}
