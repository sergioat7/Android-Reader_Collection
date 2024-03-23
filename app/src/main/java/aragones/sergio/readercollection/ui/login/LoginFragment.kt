/*
 * Copyright (c) 2023 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package aragones.sergio.readercollection.ui.login

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import aragones.sergio.readercollection.databinding.FragmentLoginBinding
import aragones.sergio.readercollection.ui.MainActivity
import aragones.sergio.readercollection.ui.base.BindingFragment
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
            LoginScreen()
        }
        setupBindings()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onDestroy()
    }
    //endregion

    //region Public methods
    fun goToRegister() {
        launchActivity(RegisterActivity::class.java)
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

        viewModel.loginError.observe(viewLifecycleOwner) { error ->

            if (error == null) {
                launchActivity(MainActivity::class.java, true)
            } else {

                hideLoading()
                manageError(error)
            }
        }
    }
    //endregion
}