package net.komunan.komunantw.ui.main.home

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.simple_view_pager.*
import net.komunan.komunantw.R
import net.komunan.komunantw.observeOnNotNull

class HomeFragment: Fragment() {
    companion object {
        @JvmStatic
        fun create() = HomeFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.simple_view_pager, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val viewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        viewModel.timelines.observeOnNotNull(this) { timelines ->
            container.adapter = HomeAdapter(fragmentManager, timelines)
        }
    }
}
