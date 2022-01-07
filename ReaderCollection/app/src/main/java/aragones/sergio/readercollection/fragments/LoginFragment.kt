/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/10/2020
 */

package aragones.sergio.readercollection.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import aragones.sergio.readercollection.activities.MainActivity
import aragones.sergio.readercollection.activities.RegisterActivity
import aragones.sergio.readercollection.base.BindingFragment
import aragones.sergio.readercollection.databinding.FragmentLoginBinding
import aragones.sergio.readercollection.extensions.afterTextChanged
import aragones.sergio.readercollection.extensions.showOrHidePassword
import aragones.sergio.readercollection.viewmodelfactories.LoginViewModelFactory
import aragones.sergio.readercollection.viewmodels.LoginViewModel
import kotlinx.android.synthetic.main.fragment_login.*

class LoginFragment : BindingFragment<FragmentLoginBinding>() {

    //region Protected properties
    override val hasOptionsMenu = false
    //endregion

    //region Private properties
    private lateinit var viewModel: LoginViewModel
    //endregion

    //region Lifecycle methods
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onDestroy()
    }
    //endregion

    //region Private methods
    private fun initializeUI() {

        val application = activity?.application ?: return
        viewModel =
            ViewModelProvider(this, LoginViewModelFactory(application))[LoginViewModel::class.java]
        setupBindings()

        with(binding) {
            editTextUsername.setText(viewModel.username)

            editTextUsername.afterTextChanged {
                loginDataChanged()
            }

            editTextPassword.afterTextChanged {
                loginDataChanged()
            }

            imageButtonPassword.setOnClickListener {
                editTextPassword.showOrHidePassword(imageButtonPassword)
            }

            buttonLogin.setOnClickListener {

                viewModel.login(
                    editTextUsername.text.toString(),
                    editTextPassword.text.toString()
                )
            }

            buttonRegister.setOnClickListener {
                launchActivity(RegisterActivity::class.java)
            }
        }
    }

    private fun setupBindings() {

        viewModel.loginFormState.observe(viewLifecycleOwner, {

            val loginState = it ?: return@observe

            with(binding) {
                buttonLogin.isEnabled = loginState.isDataValid

                if (loginState.usernameError != null) {
                    editTextUsername.error = getString(loginState.usernameError)
                }
                if (loginState.passwordError != null) {
                    editTextPassword.error = getString(loginState.passwordError)
                }
            }
        })

        viewModel.loginLoading.observe(viewLifecycleOwner, { isLoading ->

            if (isLoading) {
                showLoading()
            } else {
                hideLoading()
            }
        })

        viewModel.loginError.observe(viewLifecycleOwner, { error ->

            if (error == null) {
                launchActivity(MainActivity::class.java)
            } else {

                hideLoading()
                manageError(error)
            }
        })
    }

    private fun loginDataChanged() {

        viewModel.loginDataChanged(
            binding.editTextUsername.text.toString(),
            binding.editTextPassword.text.toString()
        )
    }
    //endregion
}