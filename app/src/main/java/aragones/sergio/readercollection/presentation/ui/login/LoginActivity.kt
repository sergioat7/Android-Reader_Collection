/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/10/2020
 */

package aragones.sergio.readercollection.presentation.ui.login

import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.core.content.ContextCompat
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.databinding.ActivityLoginBinding
import aragones.sergio.readercollection.presentation.extensions.isDarkMode
import aragones.sergio.readercollection.presentation.extensions.setStatusBarStyle
import aragones.sergio.readercollection.presentation.ui.MainActivity
import aragones.sergio.readercollection.presentation.ui.base.BaseActivity
import aragones.sergio.readercollection.presentation.ui.components.InformationAlertDialog
import aragones.sergio.readercollection.presentation.ui.register.RegisterActivity
import aragones.sergio.readercollection.presentation.ui.theme.ReaderCollectionTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : BaseActivity() {

    //region Private properties
    private val viewModel: LoginViewModel by viewModels()
    //endregion

    //region Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setStatusBarStyle(
            ContextCompat.getColor(this, R.color.colorSecondary),
            !isDarkMode(),
        )

        ActivityLoginBinding.inflate(layoutInflater).apply {
            setContentView(root)

            composeView.setContent {
                ReaderCollectionTheme {
                    val state by viewModel.uiState
                    val error by viewModel.loginError.observeAsState()

                    LoginScreen(
                        state = state,
                        onLoginDataChange = viewModel::loginDataChanged,
                        onLogin = viewModel::login,
                        onGoToRegister = viewModel::goToRegister,
                    )

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
            }
        }

        viewModel.activityName.observe(this) { activityName ->

            when (activityName) {
                MainActivity::class.simpleName -> launchActivity(MainActivity::class.java, true)
                RegisterActivity::class.simpleName -> launchActivity(RegisterActivity::class.java)
                else -> Unit
            }
        }

        onBackPressedDispatcher.addCallback {
            moveTaskToBack(true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.onDestroy()
    }
    //endregion
}