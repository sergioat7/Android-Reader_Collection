/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.presentation.ui.settings

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.databinding.FragmentSettingsBinding
import aragones.sergio.readercollection.presentation.extensions.style
import aragones.sergio.readercollection.presentation.interfaces.MenuProviderInterface
import aragones.sergio.readercollection.presentation.ui.base.BindingFragment
import aragones.sergio.readercollection.presentation.ui.components.ConfirmationAlertDialog
import aragones.sergio.readercollection.presentation.ui.components.InformationAlertDialog
import aragones.sergio.readercollection.presentation.ui.landing.LandingActivity
import com.aragones.sergio.util.StatusBarStyle
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

        binding.composeView.setContent {
            SettingsScreen(viewModel)

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

            val error by viewModel.profileError.observeAsState()
            val infoDialogMessageId by viewModel.infoDialogMessageId.observeAsState(initial = -1)

            val text = if (error != null) {
                val errorText = StringBuilder()
                if (requireNotNull(error).error.isNotEmpty()) {
                    errorText.append(requireNotNull(error).error)
                } else {
                    errorText.append(resources.getString(requireNotNull(error).errorKey))
                }
                errorText.toString()
            } else if (infoDialogMessageId != -1) {
                getString(infoDialogMessageId)
            } else {
                ""
            }
            InformationAlertDialog(show = text.isNotEmpty(), text = text) {
                viewModel.closeDialogs()
            }
        }

        viewModel.activityName.observe(viewLifecycleOwner) { activityName ->

            when (activityName) {
                LandingActivity::class.simpleName -> {
                    launchActivity(LandingActivity::class.java, true)
                    activity?.finish()
                }

                else -> Unit
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

        viewModel.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.onDestroy()
    }
    //endregion

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return false
    }

    //region Private methods
//    private fun createTargetsForToolbar(): List<TapTarget> {
//
//        val deleteProfileItem = binding.toolbar.menu.findItem(R.id.action_delete)
//        val logoutItem = binding.toolbar.menu.findItem(R.id.action_logout)
//        return listOf(
//            TapTarget.forToolbarMenuItem(
//                binding.toolbar,
//                deleteProfileItem.itemId,
//                resources.getString(R.string.delete_profile_icon_tutorial_title),
//                resources.getString(R.string.delete_profile_icon_tutorial_description)
//            ).style(requireContext()).cancelable(true).tintTarget(true),
//            TapTarget.forToolbarMenuItem(
//                binding.toolbar,
//                logoutItem.itemId,
//                resources.getString(R.string.logout_icon_tutorial_title),
//                resources.getString(R.string.logout_icon_tutorial_description)
//            ).style(requireContext()).cancelable(true).tintTarget(true)
//        )
//    }

//    private fun createTargetsForScrollView(): List<TapTarget> {
//        return listOf(
//            TapTarget.forView(
//                binding.buttonSave,
//                resources.getString(R.string.save_settings_button_tutorial_title),
//                resources.getString(R.string.save_settings_button_tutorial_description)
//            ).style(requireContext()).cancelable(true).tintTarget(false)
//        )
//    }

    private fun createSequence() {

        if (!viewModel.tutorialShown) {
            mainContentSequence = TapTargetSequence(requireActivity()).apply {
//                targets(createTargetsForScrollView())
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
//                    targets(createTargetsForToolbar())
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