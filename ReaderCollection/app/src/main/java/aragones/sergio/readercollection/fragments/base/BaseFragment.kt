/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/10/2020
 */

package aragones.sergio.readercollection.fragments.base

import android.app.AlertDialog
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.fragments.popups.PopupErrorDialogFragment
import aragones.sergio.readercollection.models.responses.ErrorResponse

open class BaseFragment: Fragment() {

    private var loadingFragment: PopupErrorDialogFragment? = null

    fun manageError(errorResponse: ErrorResponse) {

        val error = StringBuilder()
        if (errorResponse.error.isNotEmpty()) {
            error.append(errorResponse.error)
        } else {
            error.append(resources.getString(errorResponse.errorKey))
        }
        showPopupDialog(error.toString())
    }

    fun showPopupDialog(message: String) {

        val ft: FragmentTransaction = activity?.supportFragmentManager?.beginTransaction() ?: return
        val prev = activity?.supportFragmentManager?.findFragmentByTag("popupDialog")
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)
        val dialogFragment = PopupErrorDialogFragment(message)
        dialogFragment.show(ft, "popupDialog")
    }

    fun <T> launchActivity(activity: Class<T>) {

        val intent = Intent(context, activity).apply {}
        startActivity(intent)
    }

    fun showLoading() {
        //TODO show loading
    }

    fun hideLoading() {
        //TODO hide loading
    }

    fun showPopupConfirmationDialog(messageId: Int, acceptHandler: () -> Unit) {

        AlertDialog.Builder(context)
            .setMessage(resources.getString(messageId))
            .setCancelable(false)
            .setPositiveButton(resources.getString(R.string.accept)) { dialog, _ ->

                acceptHandler()
                dialog.dismiss()
            }
            .setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}