/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 28/10/2020
 */

package aragones.sergio.readercollection.presentation.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.remote.model.ErrorResponse
import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.presentation.ui.MainActivity
import aragones.sergio.readercollection.presentation.ui.base.BaseViewModel
import aragones.sergio.readercollection.presentation.ui.login.model.LoginFormState
import com.aragones.sergio.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val userRepository: UserRepository
) : BaseViewModel() {

    //region Private properties
    private val _username = MutableLiveData<String>()
    private val _password = MutableLiveData<String>()
    private val _confirmPassword = MutableLiveData<String>()
    private val _registerForm = MutableLiveData<LoginFormState>()
    private val _registerLoading = MutableLiveData<Boolean>()
    private val _registerError = MutableLiveData<ErrorResponse?>()
    private val _infoDialogMessageId = MutableLiveData(-1)
    private val _activityName = MutableLiveData<String?>()
    //endregion

    //region Public properties
    val username: LiveData<String> = _username
    val password: LiveData<String> = _password
    val confirmPassword: LiveData<String> = _confirmPassword
    val registerFormState: LiveData<LoginFormState> = _registerForm
    val registerLoading: LiveData<Boolean> = _registerLoading
    val registerError: LiveData<ErrorResponse?> = _registerError
    val infoDialogMessageId: LiveData<Int> = _infoDialogMessageId
    val activityName: LiveData<String?> = _activityName
    //endregion

    //region Lifecycle methods
    override fun onDestroy() {
        super.onDestroy()
        userRepository.onDestroy()
    }
    //endregion

    //region Public methods
    fun register(username: String, password: String) {

        _registerLoading.value = true
        userRepository.register(username, password, success = {
            userRepository.login(username, password, success = {

                _registerLoading.value = false
                _activityName.value = MainActivity::class.simpleName
                _activityName.value = null
            }, failure = {

                _registerLoading.value = false
                _registerError.value = it
            })
        }, failure = {

            _registerLoading.value = false
            _registerError.value = it
        })
    }

    fun registerDataChanged(username: String, password: String, confirmPassword: String) {

        var usernameError: Int? = null
        var passwordError: Int? = null
        var isDataValid = true

        if (!Constants.isUserNameValid(username)) {
            usernameError = R.string.invalid_username
            isDataValid = false
        }
        if (!Constants.isPasswordValid(password)) {
            passwordError = R.string.invalid_password
            isDataValid = false
        }
        if (password != confirmPassword) {
            passwordError = R.string.invalid_repeat_password
            isDataValid = false
        }

        _username.value = username
        _password.value = password
        _confirmPassword.value = confirmPassword
        _registerForm.value = LoginFormState(usernameError, passwordError, isDataValid)
    }

    fun showInfoDialog(textId: Int) {
        _infoDialogMessageId.value = textId
    }

    fun closeDialogs() {

        _registerError.value = null
        _infoDialogMessageId.value = -1
    }
    //endregion
}