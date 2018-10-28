package net.komunan.komunantw.ui.home

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.komunan.komunantw.common.observeOnNotNull
import org.jetbrains.anko.AnkoContext

class HomeFragment: Fragment() {
    companion object {
        @JvmStatic
        fun create() = HomeFragment()
    }

    private val viewModel by lazy { ViewModelProviders.of(this).get(HomeViewModel::class.java) }
    private val ui = HomeUI()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ui.createView(AnkoContext.create(context!!, this))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.run {
            columns().observeOnNotNull(this@HomeFragment) { columns ->
                ui.timelines.adapter = HomeAdapter(fragmentManager, columns)
            }
        }
    }
}
