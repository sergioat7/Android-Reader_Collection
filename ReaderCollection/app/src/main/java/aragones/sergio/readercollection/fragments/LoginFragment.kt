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
import aragones.sergio.readercollection.extensions.showOrHidePassword
import aragones.sergio.readercollection.utils.StatusBarStyle
import aragones.sergio.readercollection.viewmodelfactories.LoginViewModelFactory
import aragones.sergio.readercollection.viewmodels.LoginViewModel
import kotlinx.android.synthetic.main.fragment_login.*

class LoginFragment : BindingFragment<FragmentLoginBinding>() {

    //region Protected properties
    override val hasOptionsMenu = false
    override val statusBarStyle = StatusBarStyle.PRIMARY
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

    //region Public methods
    fun loginDataChanged() {

        viewModel.loginDataChanged(
            binding.editTextUsername.text.toString(),
            binding.editTextPassword.text.toString()
        )
    }

    fun login() {

        viewModel.login(
            binding.editTextUsername.text.toString(),
            binding.editTextPassword.text.toString()
        )
    }
    //endregion

    //region Private methods
    private fun initializeUI() {

        val application = activity?.application ?: return
        viewModel =
            ViewModelProvider(this, LoginViewModelFactory(application))[LoginViewModel::class.java]
        setupBindings()

        with(binding) {

            imageButtonPassword.setOnClickListener {
                editTextPassword.showOrHidePassword(imageButtonPassword)
            }

            buttonRegister.setOnClickListener {
                launchActivity(RegisterActivity::class.java)
            }

            fragment = this@LoginFragment
            viewModel = this@LoginFragment.viewModel
            lifecycleOwner = this@LoginFragment
        }
    }

    private fun setupBindings() {

        viewModel.loginFormState.observe(viewLifecycleOwner, {

            val loginState = it ?: return@observe

            if (loginState.usernameError != null) {
                binding.editTextUsername.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                binding.editTextPassword.error = getString(loginState.passwordError)
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
                launchActivity(MainActivity::class.java, true)
            } else {

                hideLoading()
                manageError(error)
            }
        })
    }
    //endregion
}