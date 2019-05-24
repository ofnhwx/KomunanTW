package net.komunan.komunantw.ui.common.base

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders

abstract class TWBaseFragment : Fragment() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        name?.let { name ->
            activity?.title = name
        }
    }

    open val name: String?
        get() = null

    protected fun <T : ViewModel> activityViewModel(clazz: Class<T>): T {
        return ViewModelProviders.of(activity!!).get(clazz)
    }

    protected fun <T : ViewModel> viewModel(clazz: Class<T>): T {
        return ViewModelProviders.of(this).get(clazz)
    }

    protected fun <T : ViewModel> viewModel(clazz: Class<T>, factory: ViewModelProvider.Factory): T {
        return ViewModelProviders.of(this, factory).get(clazz)
    }
}
