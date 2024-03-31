/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package aragones.sergio.readercollection.ui.settings

import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.databinding.FragmentSettingsBinding
import aragones.sergio.readercollection.presentation.extensions.doAfterTextChanged
import aragones.sergio.readercollection.presentation.extensions.getPosition
import aragones.sergio.readercollection.presentation.extensions.getValue
import aragones.sergio.readercollection.presentation.extensions.setEndIconOnClickListener
import aragones.sergio.readercollection.presentation.extensions.setError
import aragones.sergio.readercollection.presentation.extensions.setValue
import aragones.sergio.readercollection.presentation.extensions.style
import aragones.sergio.readercollection.presentation.interfaces.MenuProviderInterface
import aragones.sergio.readercollection.ui.base.BindingFragment
import aragones.sergio.readercollection.ui.components.ConfirmationAlertDialog
import aragones.sergio.readercollection.ui.components.InformationAlertDialog
import aragones.sergio.readercollection.ui.landing.LandingActivity
import com.aragones.sergio.util.CustomDropdownType
import com.aragones.sergio.util.Preferences
import com.aragones.sergio.util.StatusBarStyle
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class SettingsFragment : BindingFragment<FragmentSettingsBinding>(), MenuProviderInterface {

    //region Protected properties
    override val menuProviderInterface = this
    override val statusBarStyle = StatusBarStyle.PRIMARY
    //endregion

    //region Private properties
    private val viewModel: SettingsViewModel by viewModels()
    private lateinit var mainContentSequence: TapTargetSequence
    private var toolbarSequence: TapTargetSequence? = null
    private var mainContentSequenceShown = false
    //endregion

    //region Lifecycle methods
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar = binding.toolbar
        initializeUi()
        binding.composeView.setContent {

            val confirmationMessageId by viewModel.confirmationDialogMessageId.observeAsState(
                initial = -1
            )
            ConfirmationAlertDialog(
                show = confirmationMessageId != -1,
                textId = confirmationMessageId,
                onCancel = {
                    viewModel.closeDialogs()
                },
                onAccept = {

                    when (confirmationMessageId) {
                        R.string.profile_delete_confirmation -> {
                            viewModel.deleteUser()
                        }

                        R.string.profile_logout_confirmation -> {
                            viewModel.logout()
                        }

                        else -> Unit
                    }
                    viewModel.closeDialogs()
                })

            val infoMessageId by viewModel.infoDialogMessageId.observeAsState(initial = -1)
            val text = if (infoMessageId != -1) {
                getString(infoMessageId)
            } else {
                ""
            }
            InformationAlertDialog(show = infoMessageId != -1, text = text) {
                viewModel.closeDialogs()
            }
        }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            val locale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
            viewModel.language = locale.language
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.onDestroy()
    }
    //endregion

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

        menu.clear()
        menuInflater.inflate(R.menu.settings_toolbar_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {

        return when (menuItem.itemId) {
            R.id.action_delete -> {

                viewModel.showConfirmationDialog(R.string.profile_delete_confirmation)
                true
            }

            R.id.action_logout -> {

                viewModel.showConfirmationDialog(R.string.profile_logout_confirmation)
                true
            }

            else -> false
        }
    }

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
            this.textInputLayoutPassword.textInputEditText.clearFocus()
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

        setupBindings()

        binding.textInputLayoutUsername.setEndIconOnClickListener {
            viewModel.showInfoDialog(R.string.username_info)
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

        viewModel.profileError.observe(viewLifecycleOwner) {
            it?.let { manageError(it) }
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