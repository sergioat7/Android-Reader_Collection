/*
 * Copyright (c) 2023 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package aragones.sergio.readercollection.ui.register

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.fragment.app.viewModels
import aragones.sergio.readercollection.databinding.FragmentRegisterBinding
import aragones.sergio.readercollection.ui.MainActivity
import aragones.sergio.readercollection.ui.base.BindingFragment
import aragones.sergio.readercollection.ui.components.InformationAlertDialog
import com.aragones.sergio.util.StatusBarStyle
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment : BindingFragment<FragmentRegisterBinding>() {

    //region Protected properties
    override val menuProviderInterface = null
    override val statusBarStyle = StatusBarStyle.PRIMARY
    //endregion

    //region Private properties
    private val viewModel: RegisterViewModel by viewModels()
    //endregion

    //region Lifecycle methods
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeView.setContent {
            RegisterScreen(viewModel)

            val error by viewModel.registerError.observeAsState()
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
        setupBindings()
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.onDestroy()
    }
    //endregion

    //region Private methods
    private fun setupBindings() {

        viewModel.activityName.observe(viewLifecycleOwner) { activityName ->

            when (activityName) {
                MainActivity::class.simpleName -> launchActivity(MainActivity::class.java)
                else -> Unit
            }
        }
    }
    //endregion
}