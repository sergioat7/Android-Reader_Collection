/*
 * Copyright (c) 2023 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package aragones.sergio.readercollection.ui.register

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.databinding.FragmentRegisterBinding
import aragones.sergio.readercollection.extensions.doAfterTextChanged
import aragones.sergio.readercollection.extensions.getValue
import aragones.sergio.readercollection.extensions.setEndIconOnClickListener
import aragones.sergio.readercollection.extensions.setError
import aragones.sergio.readercollection.ui.MainActivity
import aragones.sergio.readercollection.ui.base.BindingFragment
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
        initializeUi()
    }

    override fun onResume() {
        super.onResume()

        binding.textInputLayoutUsername.doAfterTextChanged {
            registerDataChanged()
        }
        binding.textInputLayoutPassword.doAfterTextChanged {
            registerDataChanged()
        }
        binding.textInputLayoutConfirmPassword.doAfterTextChanged {
            registerDataChanged()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.onDestroy()
    }
    //endregion

    //region Public methods
    fun register() {

        binding.textInputLayoutUsername.textInputEditText.clearFocus()
        binding.textInputLayoutPassword.textInputEditText.clearFocus()
        binding.textInputLayoutConfirmPassword.textInputEditText.clearFocus()
        viewModel.register(
            binding.textInputLayoutUsername.getValue(),
            binding.textInputLayoutPassword.getValue()
        )
    }
    //endregion

    //region Protected methods
    override fun initializeUi() {
        super.initializeUi()

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

        viewModel.registerFormState.observe(viewLifecycleOwner) {

            val registerState = it ?: return@observe

            with(binding) {
                textInputLayoutUsername.setError("")
                textInputLayoutPassword.setError("")
                textInputLayoutConfirmPassword.setError("")

                if (registerState.usernameError != null) {
                    textInputLayoutUsername.setError(getString(registerState.usernameError ?: 0))
                }
                if (registerState.passwordError != null) {

                    textInputLayoutPassword.setError(getString(registerState.passwordError ?: 0))
                    textInputLayoutConfirmPassword.setError(
                        getString(
                            registerState.passwordError ?: 0
                        )
                    )
                }
            }
        }

        viewModel.registerLoading.observe(viewLifecycleOwner) { isLoading ->

            if (isLoading) {
                showLoading()
            } else {
                hideLoading()
            }
        }

        viewModel.registerError.observe(viewLifecycleOwner) { error ->

            if (error == null) {
                launchActivity(MainActivity::class.java)
            } else {
                manageError(error)
            }
        }
    }

    private fun registerDataChanged() {

        viewModel.registerDataChanged(
            binding.textInputLayoutUsername.getValue(),
            binding.textInputLayoutPassword.getValue(),
            binding.textInputLayoutConfirmPassword.getValue()
        )
    }
    //endregion
}