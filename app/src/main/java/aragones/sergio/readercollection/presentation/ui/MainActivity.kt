/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 15/10/2020
 */

package aragones.sergio.readercollection.presentation.ui

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.databinding.ActivityMainBinding
import aragones.sergio.readercollection.presentation.extensions.setupWithNavController
import aragones.sergio.readercollection.presentation.ui.base.BaseActivity
import aragones.sergio.readercollection.utils.InAppUpdateService
import com.google.android.play.core.install.model.InstallStatus
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    //region Private properties
    private lateinit var binding: ActivityMainBinding
    private var currentNavController: LiveData<NavController>? = null
    private lateinit var inAppUpdateService: InAppUpdateService
    //endregion

    //region Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            setupBottomNavigationBar()
        } // Else, need to wait for onRestoreInstanceState

        inAppUpdateService = InAppUpdateService(this)
        inAppUpdateService.installStatus.observe(this) {
            if (it == InstallStatus.DOWNLOADED) {
                inAppUpdateService.onResume()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        inAppUpdateService.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()

        inAppUpdateService.onDestroy()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Now that BottomNavigationBar has restored its instance state
        // and its selectedItemId, we can proceed with setting up the
        // BottomNavigationBar with Navigation
        setupBottomNavigationBar()
    }

    override fun onSupportNavigateUp(): Boolean = currentNavController?.value?.navigateUp() ?: false
    //endregion

    //region Private methods
    private fun setupBottomNavigationBar() {
        val bottomNavigationView = binding.navView
        val navGraphIds = listOf(
            R.navigation.nav_graph_books,
            R.navigation.nav_graph_search,
            R.navigation.nav_graph_stats,
            R.navigation.nav_graph_settings,
        )

        // Setup the bottom navigation view with a list of navigation graphs
        val controller = bottomNavigationView.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.nav_host_fragment,
            intent = intent,
        )
        currentNavController = controller
    }
    //endregion
}