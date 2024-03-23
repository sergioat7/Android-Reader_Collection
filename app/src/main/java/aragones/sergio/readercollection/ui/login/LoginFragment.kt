/*
 * Copyright (c) 2023 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package aragones.sergio.readercollection.ui.login

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.fragment.app.viewModels
import aragones.sergio.readercollection.databinding.FragmentLoginBinding
import aragones.sergio.readercollection.ui.MainActivity
import aragones.sergio.readercollection.ui.base.BindingFragment
import aragones.sergio.readercollection.ui.components.InformationAlertDialog
import aragones.sergio.readercollection.ui.register.RegisterActivity
import com.aragones.sergio.util.StatusBarStyle
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : BindingFragment<FragmentLoginBinding>() {

    //region Protected properties
    override val menuProviderInterface = null
    override val statusBarStyle = StatusBarStyle.PRIMARY
    //endregion

    //region Private properties
    private val viewModel: LoginViewModel by viewModels()
    //endregion

    //region Lifecycle methods
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeView.setContent {
            LoginScreen(viewModel)

            val error by viewModel.loginError.observeAsState()
            val errorText = StringBuilder()
            error?.let {
                if (it.error.isNotEmpty()) {
                    errorText.append(it.error)
                } else {
                    errorText.append(resources.getString(it.errorKey))
                }
            }
            InformationAlertDialog(show = error != null, text = errorText.toString()) {
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

        viewModel.loginLoading.observe(viewLifecycleOwner) { isLoading ->

            if (isLoading) {
                showLoading()
            } else {
                hideLoading()
            }
        }

        viewModel.activityName.observe(viewLifecycleOwner) { activityName ->

            when (activityName) {
                MainActivity::class.simpleName -> launchActivity(MainActivity::class.java, true)
                RegisterActivity::class.simpleName -> launchActivity(RegisterActivity::class.java)
                else -> Unit
            }
        }
    }
    //endregion
}