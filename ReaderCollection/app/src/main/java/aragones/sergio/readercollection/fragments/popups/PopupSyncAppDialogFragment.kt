/*
 * Copyright (c) 2021 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 10/1/2021
 */

package aragones.sergio.readercollection.fragments.popups

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.activities.LandingActivity
import aragones.sergio.readercollection.viewmodelfactories.PopupSyncAppViewModelFactory
import aragones.sergio.readercollection.viewmodels.PopupSyncAppViewModel

class PopupSyncAppDialogFragment : DialogFragment() {

    //region Private properties
    private lateinit var viewModel: PopupSyncAppViewModel
    //endregion

    //region Lifecycle methods
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_popup_sync_app_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onDestroy()
    }
    //endregion

    //region Private functions
    private fun initializeUI() {

        val application = activity?.application ?: return
        viewModel = ViewModelProvider(
            this,
            PopupSyncAppViewModelFactory(application)
        )[PopupSyncAppViewModel::class.java]
        setupBindings()

        viewModel.loadContent()
    }

    private fun setupBindings() {

        viewModel.loginError.observe(viewLifecycleOwner, { error ->

            if (error == null) {
                goToMainView()
            } else {
                showPopupDialog(resources.getString(error.errorKey))
            }
            dismiss()
        })
    }

    private fun goToMainView() {

        val intent = Intent(context, LandingActivity::class.java).apply {}
        startActivity(intent)
        activity?.finish()
    }

    private fun showPopupDialog(message: String) {

        val ft: FragmentTransaction = activity?.supportFragmentManager?.beginTransaction() ?: return
        val prev = activity?.supportFragmentManager?.findFragmentByTag("popupDialog")
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)
        val dialogFragment = PopupErrorDialogFragment(message)
        dialogFragment.show(ft, "popupDialog")
    }
    //endregion
}