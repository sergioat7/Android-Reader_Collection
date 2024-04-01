/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 10/1/2021
 */

package aragones.sergio.readercollection.presentation.ui.modals.syncapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.ui.landing.LandingActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PopupSyncAppDialogFragment : DialogFragment() {

    //region Private properties
    private val viewModel: PopupSyncAppViewModel by viewModels()
    //endregion

    //region Lifecycle methods
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_fragment_popup_sync_app, container, false)
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

        setupBindings()

        viewModel.loadContent()
    }

    private fun setupBindings() {

        viewModel.loginError.observe(viewLifecycleOwner) { error ->

            if (error == null) {
                goToMainView()
            } else {
                showPopupDialog(resources.getString(error.errorKey))
            }
            dismiss()
        }
    }

    private fun goToMainView() {

        val intent = Intent(context, LandingActivity::class.java).apply {}
        startActivity(intent)
        activity?.finish()
    }

    private fun showPopupDialog(message: String) {

        MaterialAlertDialogBuilder(
            requireContext(),
            R.style.ThemeOverlay_ReaderCollection_MaterialAlertDialog
        )
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(resources.getString(R.string.accept)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    //endregion
}