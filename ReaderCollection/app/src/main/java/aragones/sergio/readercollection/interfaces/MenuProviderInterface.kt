/*
 * Copyright (c) 2023 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 11/10/2023
 */

package aragones.sergio.readercollection.interfaces

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem

interface MenuProviderInterface {

    fun onCreateMenu(menu: Menu, menuInflater: MenuInflater)
    fun onMenuItemSelected(menuItem: MenuItem): Boolean
}