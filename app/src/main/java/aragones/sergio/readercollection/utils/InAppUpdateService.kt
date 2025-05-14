/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/12/2023
 */

package aragones.sergio.readercollection.utils

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.remote.di.IoDispatcher
import aragones.sergio.readercollection.domain.UserRepository
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.installStatus
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InAppUpdateService @Inject constructor(
    private val activity: ComponentActivity,
    private val userRepository: UserRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    //region Private properties
    private val appUpdateManager = AppUpdateManagerFactory.create(activity)
    private val listener: InstallStateUpdatedListener
    private var appUpdateType = AppUpdateType.FLEXIBLE
    private val _installStatus = MutableStateFlow(InstallStatus.UNKNOWN)
    private val inAppUpdateLauncher: ActivityResultLauncher<IntentSenderRequest>
    //endregion

    //region Public properties
    val installStatus: StateFlow<Int> = _installStatus
    //endregion

    //region Lifecycle methods
    init {

        listener = InstallStateUpdatedListener { state ->
            _installStatus.value = state.installStatus
        }
        appUpdateManager.registerListener(listener)

        inAppUpdateLauncher =
            activity.registerForActivityResult(
                ActivityResultContracts.StartIntentSenderForResult(),
            ) {
                if (it.resultCode == AppCompatActivity.RESULT_OK && isImmediateUpdate()) {
                    _installStatus.value = InstallStatus.INSTALLED
                } else if (it.resultCode != AppCompatActivity.RESULT_OK) {
                    _installStatus.value = InstallStatus.CANCELED
                }
            }
    }
    //endregion

    //region Public methods
    fun checkVersion() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->

            if (isUpdateDownloading(info)) {
                _installStatus.value = InstallStatus.DOWNLOADED
            } else if (isUpdateAvailable(info)) {
                CoroutineScope(ioDispatcher).launch {
                    startUpdate(info, AppUpdateType.FLEXIBLE)
                }
            } else {
                _installStatus.value = InstallStatus.INSTALLED
            }
        }
        appUpdateManager.appUpdateInfo.addOnFailureListener {
            _installStatus.value = InstallStatus.INSTALLED
        }
    }

    fun onResume() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->

            if (isFlexibleUpdate() && isUpdateAlreadyInstalled(info)) {
                flexibleUpdateDownloadCompleted()
            } else if (isImmediateUpdate() && isUpdateDownloading(info)) {
                startUpdate(info, AppUpdateType.IMMEDIATE)
            }
        }
    }

    fun onDestroy() {
        appUpdateManager.unregisterListener(listener)
    }
    //endregion

    //region Private methods
    private fun isImmediateUpdate() = appUpdateType == AppUpdateType.IMMEDIATE

    private fun isFlexibleUpdate() = appUpdateType == AppUpdateType.FLEXIBLE

    private fun isUpdateAlreadyInstalled(info: AppUpdateInfo) =
        info.installStatus == InstallStatus.DOWNLOADED

    private fun isUpdateAvailable(info: AppUpdateInfo) =
        info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE

    private fun isUpdateDownloading(info: AppUpdateInfo) =
        info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS

    private fun startUpdate(info: AppUpdateInfo, type: Int) {
        appUpdateManager.startUpdateFlowForResult(
            info,
            inAppUpdateLauncher,
            AppUpdateOptions.newBuilder(type).build(),
        )
        appUpdateType = type
    }

    private fun flexibleUpdateDownloadCompleted() {
        Snackbar
            .make(
                activity.findViewById(android.R.id.content),
                activity.getString(R.string.message_app_update_downloaded),
                Snackbar.LENGTH_INDEFINITE,
            ).apply {
                setAction(
                    activity.getString(R.string.restart),
                ) { appUpdateManager.completeUpdate() }
                setBackgroundTint(activity.getColor(R.color.colorPrimary))
                setActionTextColor(activity.getColor(R.color.colorSecondary))
                show()
            }
    }
    //endregion
}