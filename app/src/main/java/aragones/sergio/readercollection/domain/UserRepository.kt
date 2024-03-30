/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 6/11/2020
 */

package aragones.sergio.readercollection.domain

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.local.SharedPreferencesHandler
import aragones.sergio.readercollection.data.local.model.AuthData
import aragones.sergio.readercollection.data.local.model.UserData
import aragones.sergio.readercollection.data.remote.di.MainDispatcher
import aragones.sergio.readercollection.data.remote.model.ErrorResponse
import aragones.sergio.readercollection.data.remote.services.UserApiService
import aragones.sergio.readercollection.domain.base.BaseRepository
import com.aragones.sergio.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import java.util.Locale
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val api: UserApiService,
    @ApplicationContext private val context: Context,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher
) : BaseRepository() {

    //region Private properties
    private val externalScope = CoroutineScope(Job() + mainDispatcher)
    private var localeManager: LocaleManager? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java)
        } else {
            null
        }
    //endregion

    //region Public properties
    val username: String
        get() = SharedPreferencesHandler.userData.username

    val userData: UserData
        get() = SharedPreferencesHandler.userData

    val language: String
        get() = SharedPreferencesHandler.language

    val sortParam: String?
        get() = SharedPreferencesHandler.sortParam

    val isSortDescending: Boolean
        get() = SharedPreferencesHandler.isSortDescending

    val themeMode: Int
        get() = SharedPreferencesHandler.themeMode

    val hasBooksTutorialBeenShown: Boolean
        get() = SharedPreferencesHandler.hasBooksTutorialBeenShown

    val hasDragTutorialBeenShown: Boolean
        get() = SharedPreferencesHandler.hasDragTutorialBeenShown

    val hasSearchTutorialBeenShown: Boolean
        get() = SharedPreferencesHandler.hasSearchTutorialBeenShown

    val hasStatisticsTutorialBeenShown: Boolean
        get() = SharedPreferencesHandler.hasStatisticsTutorialBeenShown

    val hasSettingsTutorialBeenShown: Boolean
        get() = SharedPreferencesHandler.hasSettingsTutorialBeenShown

    val hasNewBookTutorialBeenShown: Boolean
        get() = SharedPreferencesHandler.hasNewBookTutorialBeenShown

    val hasBookDetailsTutorialBeenShown: Boolean
        get() = SharedPreferencesHandler.hasBookDetailsTutorialBeenShown
    //endregion

    //region Public methods
    fun login(
        username: String,
        password: String,
        success: (String) -> Unit,
        failure: (ErrorResponse) -> Unit
    ) {
        if (username == Constants.GOOGLE_USER_TEST && password == Constants.GOOGLE_PASSWORD_TEST) {
            SharedPreferencesHandler.userData = UserData(
                Constants.GOOGLE_USER_TEST,
                Constants.GOOGLE_PASSWORD_TEST,
                false
            )
            success("-")
        } else if (userData.username == username && userData.password == password) {
            success("-")
        } else {
            failure(ErrorResponse(Constants.EMPTY_VALUE, R.string.wrong_credentials))
        }
//        externalScope.launch {
//
//            val body = LoginCredentials(username, password)
//            try {
//                when (val response = ApiManager.validateResponse(api.login(body))) {
//                    is RequestResult.JsonSuccess -> success(response.body.token)
//                    is RequestResult.Failure -> failure(response.error)
//                    else -> failure(ErrorResponse(Constants.EMPTY_VALUE, R.string.error_server))
//                }
//            } catch (e: Exception) {
//                failure(ErrorResponse(Constants.EMPTY_VALUE, R.string.error_server))
//            }
//        }
    }

    fun logout() {
        SharedPreferencesHandler.logout()
//        externalScope.launch {
//
//            try {
//                api.logout()
//            } catch (e: Exception) {
//            }
//        }
    }

    fun register(
        username: String,
        password: String,
        success: () -> Unit,
        failure: (ErrorResponse) -> Unit
    ) {
        SharedPreferencesHandler.userData = UserData(username, password, false)
        success()
//        externalScope.launch {
//
//            val body = LoginCredentials(username, password)
//            try {
//                when (val response = ApiManager.validateResponse(api.register(body))) {
//                    is RequestResult.Success -> success()
//                    is RequestResult.Failure -> failure(response.error)
//                    else -> failure(ErrorResponse("", R.string.error_server))
//                }
//            } catch (e: Exception) {
//                failure(ErrorResponse("", R.string.error_server))
//            }
//        }
    }

    fun updatePassword(password: String, success: () -> Unit, failure: (ErrorResponse) -> Unit) {
        success()
//        externalScope.launch {
//
//            try {
//                val body = NewPassword(password)
//                when (val response = ApiManager.validateResponse(api.updatePassword(body))) {
//                    is RequestResult.Success -> success()
//                    is RequestResult.Failure -> failure(response.error)
//                    else -> failure(ErrorResponse(Constants.EMPTY_VALUE, R.string.error_server))
//                }
//            } catch (e: Exception) {
//                failure(ErrorResponse(Constants.EMPTY_VALUE, R.string.error_server))
//            }
//        }
    }

    fun deleteUser(success: () -> Unit, failure: (ErrorResponse) -> Unit) {
        success()
//        externalScope.launch {
//
//            try {
//                when (val response = ApiManager.validateResponse(api.deleteUser())) {
//                    is RequestResult.Success -> success()
//                    is RequestResult.Failure -> failure(response.error)
//                    else -> failure(ErrorResponse(Constants.EMPTY_VALUE, R.string.error_server))
//                }
//            } catch (e: Exception) {
//                failure(ErrorResponse(Constants.EMPTY_VALUE, R.string.error_server))
//            }
//        }
    }

    fun storeLoginData(userData: UserData, authData: AuthData) {

        SharedPreferencesHandler.userData = userData
        SharedPreferencesHandler.credentials = authData
    }

    fun storeCredentials(authData: AuthData) {
        SharedPreferencesHandler.credentials = authData
    }

    fun removeCredentials() {
        SharedPreferencesHandler.removeCredentials()
    }

    fun storePassword(newPassword: String) {
        SharedPreferencesHandler.storePassword(newPassword)
    }

    fun removeUserData() {
        SharedPreferencesHandler.removeUserData()
    }

//    fun removePassword() {
//        SharedPreferencesHandler.removePassword()
//    }

    fun storeLanguage(language: String) {

        SharedPreferencesHandler.language = language
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            localeManager?.applicationLocales = LocaleList(Locale.forLanguageTag(language))
        }
    }

    fun storeSortParam(sortParam: String?) {
        SharedPreferencesHandler.sortParam = sortParam
    }

    fun storeIsSortDescending(isSortDescending: Boolean) {
        SharedPreferencesHandler.isSortDescending = isSortDescending
    }

    fun storeThemeMode(themeMode: Int) {
        SharedPreferencesHandler.themeMode = themeMode
    }

    fun setHasBooksTutorialBeenShown(hasBooksTutorialBeenShown: Boolean) {
        SharedPreferencesHandler.hasBooksTutorialBeenShown = hasBooksTutorialBeenShown
    }

    fun setHasDragTutorialBeenShown(hasDragTutorialBeenShown: Boolean) {
        SharedPreferencesHandler.hasDragTutorialBeenShown = hasDragTutorialBeenShown
    }

    fun setHasSearchTutorialBeenShown(hasSearchTutorialBeenShown: Boolean) {
        SharedPreferencesHandler.hasSearchTutorialBeenShown = hasSearchTutorialBeenShown
    }

    fun setHasStatisticsTutorialBeenShown(hasStatisticsTutorialBeenShown: Boolean) {
        SharedPreferencesHandler.hasStatisticsTutorialBeenShown = hasStatisticsTutorialBeenShown
    }

    fun setHasSettingsTutorialBeenShown(hasSettingsTutorialBeenShown: Boolean) {
        SharedPreferencesHandler.hasSettingsTutorialBeenShown = hasSettingsTutorialBeenShown
    }

    fun setHasNewBookTutorialBeenShown(hasNewBookTutorialBeenShown: Boolean) {
        SharedPreferencesHandler.hasNewBookTutorialBeenShown = hasNewBookTutorialBeenShown
    }

    fun setHasBookDetailsTutorialBeenShown(hasBookDetailsTutorialBeenShown: Boolean) {
        SharedPreferencesHandler.hasBookDetailsTutorialBeenShown = hasBookDetailsTutorialBeenShown
    }
    //endregion
}