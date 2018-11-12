package net.komunan.komunantw.common

import android.os.Bundle
import androidx.fragment.app.Fragment

abstract class TWBaseFragment: Fragment() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fragmentName()?.let { name ->
            activity?.title = name
        }
    }

    abstract fun fragmentName(): String?
}
