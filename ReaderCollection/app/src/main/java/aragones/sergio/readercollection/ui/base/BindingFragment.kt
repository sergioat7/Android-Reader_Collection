/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 7/1/2022
 */

package aragones.sergio.readercollection.ui.base

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.extensions.isDarkMode
import aragones.sergio.readercollection.extensions.setStatusBarStyle
import aragones.sergio.readercollection.ui.modals.loading.PopupLoadingDialogFragment
import aragones.sergio.readercollection.ui.modals.syncapp.PopupSyncAppDialogFragment
import aragones.sergio.readercollection.models.ErrorResponse
import aragones.sergio.readercollection.utils.StatusBarStyle
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
    protected abstract val statusBarStyle: StatusBarStyle
    protected open var toolbar: Toolbar? = null
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
        binding.root.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.colorSecondary
            )
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let {
            when (statusBarStyle) {
                StatusBarStyle.PRIMARY -> {
                    it.window.setStatusBarStyle(
                        ContextCompat.getColor(it, R.color.colorSecondary),
                        !it.isDarkMode()
                    )
                }
                StatusBarStyle.SECONDARY -> {
                    it.window.setStatusBarStyle(
                        ContextCompat.getColor(it, R.color.colorPrimary),
                        it.isDarkMode()
                    )
                }
            }
        }
    }
    //endregion

    //region Protected methods
    protected open fun initializeUi() {
        toolbar?.let {
            (activity as? AppCompatActivity)?.setSupportActionBar(it)
            it.setNavigationOnClickListener {
                findNavController().popBackStack()
            }
        }
    }
    //endregion

    //region Public methods
    fun manageError(errorResponse: ErrorResponse, goBack: MutableLiveData<Boolean>? = null) {

        val error = StringBuilder()
        if (errorResponse.error.isNotEmpty()) {
            error.append(errorResponse.error)
        } else {
            error.append(resources.getString(errorResponse.errorKey))
        }
        showPopupDialog(error.toString(), goBack)
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

    fun <T> launchActivityWithExtras(
        activity: Class<T>,
        params: Map<String, Serializable>,
        clearStack: Boolean = false
    ) {

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

            (activity?.getSystemService(Context.SEARCH_SERVICE) as? SearchManager)?.let {
                searchView.setSearchableInfo(it.getSearchableInfo(activity?.componentName))
            }

            searchView.setIconifiedByDefault(false)
            searchView.queryHint = resources.getString(R.string.search)
            if (query.isNotBlank()) {
                searchView.setQuery(query, false)
            }

            searchView.findViewById<AppCompatImageView>(androidx.appcompat.R.id.search_mag_icon)?.imageTintList =
                ColorStateList.valueOf(color)

            searchView.findViewById<View>(androidx.appcompat.R.id.search_plate)?.let { searchPlate ->

                val searchText =
                    searchPlate.findViewById<TextView>(androidx.appcompat.R.id.search_src_text)
                if (searchText != null) {

                    searchText.setTextColor(color)
                    searchText.setHintTextColor(color)
                }

                searchPlate.findViewById<AppCompatImageView>(androidx.appcompat.R.id.search_close_btn)?.imageTintList =
                    ColorStateList.valueOf(color)
            }
        }
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