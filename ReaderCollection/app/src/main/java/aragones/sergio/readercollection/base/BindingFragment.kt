/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 7/1/2022
 */

package aragones.sergio.readercollection.base

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.MutableLiveData
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.fragments.popups.PopupLoadingDialogFragment
import aragones.sergio.readercollection.fragments.popups.PopupSyncAppDialogFragment
import aragones.sergio.readercollection.models.responses.ErrorResponse
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.Serializable
import java.lang.reflect.ParameterizedType

abstract class BindingFragment<Binding : ViewDataBinding> : Fragment() {

    //region Public properties
    var searchView: SearchView? = null
    //endregion

    //region Protected properties
    protected lateinit var binding: Binding
        private set

    protected abstract val hasOptionsMenu: Boolean
    //endregion

    //region Private properties
    private var loadingFragment: PopupLoadingDialogFragment? = null
    //endregion

    //region Lifecycle methods
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        setHasOptionsMenu(hasOptionsMenu)
        val bindingType =
            (this.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments
                .firstOrNull {
                    (it as? Class<*>)?.let { clazz ->
                        ViewDataBinding::class.java.isAssignableFrom(
                            clazz
                        )
                    } == true
                }
                ?: error("Class is not parametrized with ViewDataBinding subclass")
        val inflateMethod = (bindingType as Class<*>).getMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.javaPrimitiveType
        )
        @Suppress("UNCHECKED_CAST")
        binding = inflateMethod.invoke(null, inflater, container, false) as Binding
        return binding.root
    }
    //endregion

    //region Public methods
    fun manageError(errorResponse: ErrorResponse) {

        val error = StringBuilder()
        if (errorResponse.error.isNotEmpty()) {
            error.append(errorResponse.error)
        } else {
            error.append(resources.getString(errorResponse.errorKey))
        }
        showPopupDialog(error.toString())
    }

    fun showPopupDialog(message: String, goBack: MutableLiveData<Boolean>? = null) {

        MaterialAlertDialogBuilder(
            requireContext(),
            R.style.ThemeOverlay_ReaderCollection_MaterialAlertDialog
        )
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(resources.getString(R.string.accept)) { dialog, _ ->

                dialog.dismiss()
                goBack?.let {
                    it.value = true
                }
            }
            .show()
    }

    fun <T> launchActivity(activity: Class<T>, clearStack: Boolean = false) {

        val intent = Intent(context, activity)
        if (clearStack) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    fun <T> launchActivityWithExtras(activity: Class<T>, params: Map<String, Serializable>, clearStack: Boolean = false) {

        val intent = Intent(context, activity)
        for (param in params) {
            intent.putExtra(param.key, param.value)
        }
        if (clearStack) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    fun showLoading() {

        val ft: FragmentTransaction = activity?.supportFragmentManager?.beginTransaction() ?: return
        val prev = activity?.supportFragmentManager?.findFragmentByTag("loadingDialog")
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)
        loadingFragment = PopupLoadingDialogFragment()
        loadingFragment?.let {
            it.isCancelable = false
            it.show(ft, "loadingDialog")
        }
    }

    fun hideLoading() {

        loadingFragment?.dismiss()
        loadingFragment = null
    }

    fun showPopupConfirmationDialog(
        messageId: Int,
        acceptHandler: () -> Unit,
        cancelHandler: (() -> Unit)? = null
    ) {

        MaterialAlertDialogBuilder(
            requireContext(),
            R.style.ThemeOverlay_ReaderCollection_MaterialAlertDialog
        )
            .setMessage(resources.getString(messageId))
            .setCancelable(false)
            .setPositiveButton(resources.getString(R.string.accept)) { dialog, _ ->

                acceptHandler()
                dialog.dismiss()
            }
            .setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ ->
                cancelHandler?.invoke()
                dialog.dismiss()
            }
            .show()
    }

    fun openSyncPopup() {

        showPopupConfirmationDialog(R.string.sync_confirmation, acceptHandler = {
            showSyncPopup()
        })
    }

    fun setupSearchView(colorId: Int, query: String) {

        val color = ContextCompat.getColor(requireActivity(), colorId)

        searchView?.let { searchView ->

            val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager?
            if (searchManager != null) {
                searchView.setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))
            }

            searchView.isIconified = false
            searchView.isIconifiedByDefault = false
            searchView.queryHint = resources.getString(R.string.search)
            if (query.isNotBlank()) {
                searchView.setQuery(query, false)
            }

            val searchIconId = searchView.context.resources.getIdentifier(
                "android:id/search_mag_icon",
                null,
                null
            )
            searchView.findViewById<AppCompatImageView>(searchIconId)?.imageTintList =
                ColorStateList.valueOf(color)

            val searchPlateId = searchView.context.resources.getIdentifier(
                "android:id/search_plate",
                null,
                null
            )
            val searchPlate = searchView.findViewById<View>(searchPlateId)
            if (searchPlate != null) {

                val searchTextId = searchPlate.context.resources.getIdentifier(
                    "android:id/search_src_text",
                    null,
                    null
                )
                val searchText = searchPlate.findViewById<TextView>(searchTextId)
                if (searchText != null) {

                    searchText.setTextColor(color)
                    searchText.setHintTextColor(color)
                }

                val searchCloseId = searchPlate.context.resources.getIdentifier(
                    "android:id/search_close_btn",
                    null,
                    null
                )
                searchPlate.findViewById<AppCompatImageView>(searchCloseId)?.imageTintList =
                    ColorStateList.valueOf(color)
            }
        }
    }

    fun setTitle(title: String) {
        (activity as AppCompatActivity?)?.supportActionBar?.title = title
    }

    fun setSubtitle(subtitle: String) {
        (activity as AppCompatActivity?)?.supportActionBar?.subtitle = subtitle
    }
    //endregion

    //region Private methods
    private fun showSyncPopup() {

        val ft: FragmentTransaction = activity?.supportFragmentManager?.beginTransaction() ?: return
        val prev = activity?.supportFragmentManager?.findFragmentByTag("syncDialog")
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)
        val dialogFragment = PopupSyncAppDialogFragment()
        dialogFragment.isCancelable = false
        dialogFragment.show(ft, "syncDialog")
    }
    //endregion
}