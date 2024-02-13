/*
 * Copyright (c) 2023 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/12/2023
 */

package aragones.sergio.readercollection.utils

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import aragones.sergio.readercollection.R
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.installStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.Permissions

class InAppUpdateService(
    private val activity: ComponentActivity
) {

    //region Private properties
    private val appUpdateManager = AppUpdateManagerFactory.create(activity)
    private val listener: InstallStateUpdatedListener
    private var appUpdateType = AppUpdateType.FLEXIBLE
    private val _installStatus = MutableLiveData<Int>()
    private val inAppUpdateLauncher: ActivityResultLauncher<IntentSenderRequest>
    //endregion

    //region Public properties
    val installStatus: LiveData<Int> = _installStatus
    //endregion

    //region Lifecycle methods
    init {

        listener = InstallStateUpdatedListener { state ->
            _installStatus.value = state.installStatus
        }
        appUpdateManager.registerListener(listener)

        inAppUpdateLauncher = activity.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if (it.resultCode == AppCompatActivity.RESULT_OK && appUpdateType == AppUpdateType.IMMEDIATE) {
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

            if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                _installStatus.value = InstallStatus.DOWNLOADED
            } else if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {

                CoroutineScope(Dispatchers.IO).launch {
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

            if (appUpdateType == AppUpdateType.FLEXIBLE && info.installStatus == InstallStatus.DOWNLOADED) {
                flexibleUpdateDownloadCompleted()
            } else if (appUpdateType == AppUpdateType.IMMEDIATE && info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                startUpdate(info, AppUpdateType.IMMEDIATE)
            }
        }
    }

    fun onDestroy() {
        appUpdateManager.unregisterListener(listener)
    }
    //endregion

    //region Private methods
    private fun startUpdate(info: AppUpdateInfo, type: Int) {

        appUpdateManager.startUpdateFlowForResult(
            info,
            inAppUpdateLauncher,
            AppUpdateOptions.newBuilder(type).build()
        )
        appUpdateType = type
    }

    private fun flexibleUpdateDownloadCompleted() {

        Snackbar.make(
            activity.findViewById(R.id.container),
            activity.getString(R.string.message_app_update_downloaded),
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction(activity.getString(R.string.restart)) { appUpdateManager.completeUpdate() }
            setBackgroundTint(activity.getColor(R.color.colorPrimary))
            setActionTextColor(activity.getColor(R.color.colorSecondary))
            show()
        }
    }
    //endregion
}