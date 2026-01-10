/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 11/1/2026
 */

package aragones.sergio.readercollection.presentation.navigation

interface Navigator {
    fun goToLanding()
    fun goToMain(withOptions: Boolean = true)
}