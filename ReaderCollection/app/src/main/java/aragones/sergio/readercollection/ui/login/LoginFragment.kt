/*
 * Copyright (c) 2023 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package aragones.sergio.readercollection.ui.login

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import aragones.sergio.readercollection.databinding.FragmentLoginBinding
import aragones.sergio.readercollection.extensions.doAfterTextChanged
import aragones.sergio.readercollection.extensions.getValue
import aragones.sergio.readercollection.extensions.setError
import aragones.sergio.readercollection.ui.MainActivity
import aragones.sergio.readercollection.ui.base.BindingFragment
import aragones.sergio.readercollection.ui.register.RegisterActivity
import aragones.sergio.readercollection.utils.StatusBarStyle

class LoginFragment : BindingFragment<FragmentLoginBinding>() {

    //region Protected properties
    override val hasOptionsMenu = false
    override val statusBarStyle = StatusBarStyle.PRIMARY
    //endregion

    //region Private properties
    private val viewModel: LoginViewModel by viewModels()
    //endregion

    //region Lifecycle methods
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeUi()
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

    fun login() {

        binding.textInputLayoutUsername.textInputEditText.clearFocus()
        binding.textInputLayoutPassword.textInputEditText.clearFocus()
        viewModel.login(
            binding.textInputLayoutUsername.getValue(),
            binding.textInputLayoutPassword.getValue()
        )
    }
    //endregion

    //region Protected methods
    override fun initializeUi() {

        setupBindings()

        binding.fragment = this
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
    }

    override fun onResume() {
        super.onResume()

        binding.textInputLayoutUsername.doAfterTextChanged {
            loginDataChanged()
        }
        binding.textInputLayoutPassword.doAfterTextChanged {
            loginDataChanged()
        }
    }
    //endregion

    //region Private methods
    private fun setupBindings() {

        viewModel.loginFormState.observe(viewLifecycleOwner) {

            binding.textInputLayoutUsername.setError("")
            binding.textInputLayoutPassword.setError("")
            val loginState = it ?: return@observe

            if (loginState.usernameError != null) {
                binding.textInputLayoutUsername.setError(getString(loginState.usernameError))
            }
            if (loginState.passwordError != null) {
                binding.textInputLayoutPassword.setError(getString(loginState.passwordError))
            }
        }

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

    private fun loginDataChanged() {

        viewModel.loginDataChanged(
            binding.textInputLayoutUsername.getValue(),
            binding.textInputLayoutPassword.getValue()
        )
    }
    //endregion
}