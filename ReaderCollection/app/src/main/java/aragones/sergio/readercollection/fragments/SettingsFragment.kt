/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.activities.LandingActivity
import aragones.sergio.readercollection.base.BindingFragment
import aragones.sergio.readercollection.databinding.FragmentSettingsBinding
import aragones.sergio.readercollection.extensions.*
import aragones.sergio.readercollection.utils.CustomDropdownType
import aragones.sergio.readercollection.utils.Preferences
import aragones.sergio.readercollection.utils.StatusBarStyle
import aragones.sergio.readercollection.viewmodelfactories.SettingsViewModelFactory
import aragones.sergio.readercollection.viewmodels.SettingsViewModel
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SettingsFragment : BindingFragment<FragmentSettingsBinding>() {

    //region Protected properties
    override val hasOptionsMenu = true
    override val statusBarStyle = StatusBarStyle.PRIMARY
    //endregion

    //region Private properties
    private lateinit var viewModel: SettingsViewModel
    private lateinit var mainContentSequence: TapTargetSequence
    private var toolbarSequence: TapTargetSequence? = null
    private var mainContentSequenceShown = false
    //endregion

    //region Lifecycle methods
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar = binding.toolbar
        initializeUi()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        menu.clear()
        inflater.inflate(R.menu.settings_toolbar_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_delete -> {

                showPopupConfirmationDialog(R.string.profile_delete_confirmation, acceptHandler = {
                    viewModel.deleteUser()
                })
                return true
            }
            R.id.action_logout -> {

                showPopupConfirmationDialog(R.string.profile_logout_confirmation, acceptHandler = {
                    viewModel.logout()
                })
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()

        createSequence()
    }

    override fun onStop() {
        super.onStop()

        if (!viewModel.tutorialShown) {
            mainContentSequence.cancel()
            toolbarSequence?.cancel()
        }
    }

    override fun onResume() {
        super.onResume()

        binding.textInputLayoutPassword.doAfterTextChanged {
            viewModel.profileDataChanged(it.toString())
        }
        setupDropdowns()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::viewModel.isInitialized) viewModel.onDestroy()
    }
    //endregion

    //region Public methods
    fun save() {
        with(binding) {

            val language =
                if (radioButtonEn.isChecked) Preferences.ENGLISH_LANGUAGE_KEY
                else Preferences.SPANISH_LANGUAGE_KEY
            val sortParam =
                if (dropdownTextInputLayoutSortParams.getPosition() == 0) null
                else resources.getStringArray(R.array.sorting_param_keys)[dropdownTextInputLayoutSortParams.getPosition()]
            val isSortDescending = dropdownTextInputLayoutSortOrders.getPosition() == 1
            val themeMode = dropdownTextInputLayoutAppTheme.getPosition()
            this@SettingsFragment.viewModel.save(
                textInputLayoutPassword.getValue(),
                language,
                sortParam,
                isSortDescending,
                themeMode
            )
        }
    }
    //endregion

    //region Protected methods
    override fun initializeUi() {
        super.initializeUi()

        val application = activity?.application ?: return
        viewModel = ViewModelProvider(
            this,
            SettingsViewModelFactory(application)
        )[SettingsViewModel::class.java]
        setupBindings()

        binding.textInputLayoutUsername.setEndIconOnClickListener {
            showPopupDialog(resources.getString(R.string.username_info))
        }

        binding.fragment = this
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = this
    }
    //endregion

    //region Private methods
    private fun setupBindings() {

        viewModel.profileForm.observe(viewLifecycleOwner, Observer {

            binding.textInputLayoutPassword.setError("")

            val passwordError = it ?: return@Observer
            binding.textInputLayoutPassword.setError(getString(passwordError))
        })

        viewModel.profileRedirection.observe(viewLifecycleOwner, Observer { redirect ->

            if (!redirect) return@Observer
            launchActivity(LandingActivity::class.java, true)
            activity?.finish()
        })

        viewModel.profileLoading.observe(viewLifecycleOwner) { isLoading ->

            if (isLoading) {
                showLoading()
            } else {
                hideLoading()
            }
        }

        viewModel.profileError.observe(viewLifecycleOwner) { error ->
            manageError(error)
        }
    }

    private fun setupDropdowns() {

        val sortParamKeys = resources.getStringArray(R.array.sorting_param_keys).toList()
        binding.dropdownTextInputLayoutSortParams.setValue(
            viewModel.sortParam ?: sortParamKeys[0],
            CustomDropdownType.SORT_PARAM
        )

        val sortOrderKeys = resources.getStringArray(R.array.sorting_order_keys).toList()
        binding.dropdownTextInputLayoutSortOrders.setValue(
            if (viewModel.isSortDescending) sortOrderKeys[1] else sortOrderKeys[0],
            CustomDropdownType.SORT_ORDER
        )

        val appThemes = resources.getStringArray(R.array.app_theme_values).toList()
        binding.dropdownTextInputLayoutAppTheme.setValue(
            appThemes[viewModel.themeMode],
            CustomDropdownType.APP_THEME
        )
    }

    private fun createTargetsForToolbar(): List<TapTarget> {

        val deleteProfileItem = binding.toolbar.menu.findItem(R.id.action_delete)
        val logoutItem = binding.toolbar.menu.findItem(R.id.action_logout)
        return listOf(
            TapTarget.forToolbarMenuItem(
                binding.toolbar,
                deleteProfileItem.itemId,
                resources.getString(R.string.delete_profile_icon_tutorial_title),
                resources.getString(R.string.delete_profile_icon_tutorial_description)
            ).style(requireContext()).cancelable(true).tintTarget(true),
            TapTarget.forToolbarMenuItem(
                binding.toolbar,
                logoutItem.itemId,
                resources.getString(R.string.logout_icon_tutorial_title),
                resources.getString(R.string.logout_icon_tutorial_description)
            ).style(requireContext()).cancelable(true).tintTarget(true)
        )
    }

    private fun createTargetsForScrollView(): List<TapTarget> {
        return listOf(
            TapTarget.forView(
                binding.buttonSave,
                resources.getString(R.string.save_settings_button_tutorial_title),
                resources.getString(R.string.save_settings_button_tutorial_description)
            ).style(requireContext()).cancelable(true).tintTarget(false)
        )
    }

    private fun createSequence() {

        if (!viewModel.tutorialShown) {
            mainContentSequence = TapTargetSequence(requireActivity()).apply {
                targets(createTargetsForScrollView())
                continueOnCancel(false)
                listener(object : TapTargetSequence.Listener {
                    override fun onSequenceFinish() {
                        mainContentSequenceShown = true
                        toolbarSequence?.start()
                    }

                    override fun onSequenceStep(lastTarget: TapTarget, targetClicked: Boolean) {}

                    override fun onSequenceCanceled(lastTarget: TapTarget) {}
                })
                if (!mainContentSequenceShown) {
                    start()
                }
            }
            /*
            Must be created with a delay in order to wait for the fragment menu creation,
            otherwise it wouldn't be icons in the toolbar
             */
            lifecycleScope.launch(Dispatchers.Main) {
                delay(500)
                toolbarSequence = TapTargetSequence(requireActivity()).apply {
                    targets(createTargetsForToolbar())
                    continueOnCancel(false)
                    listener(object : TapTargetSequence.Listener {
                        override fun onSequenceFinish() {
                            viewModel.setTutorialAsShown()
                        }

                        override fun onSequenceStep(
                            lastTarget: TapTarget,
                            targetClicked: Boolean
                        ) {
                        }

                        override fun onSequenceCanceled(lastTarget: TapTarget) {}
                    })
                    if (mainContentSequenceShown) {
                        start()
                    }
                }
            }
        }
    }
    //endregion
}