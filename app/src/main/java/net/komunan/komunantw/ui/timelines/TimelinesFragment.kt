package net.komunan.komunantw.ui.timelines

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.komunan.komunantw.common.observeOnNotNull
import org.jetbrains.anko.AnkoContext

class TimelinesFragment: Fragment() {
    companion object {
        @JvmStatic
        fun create() = TimelinesFragment()
    }

    private val viewModel by lazy { ViewModelProviders.of(this).get(TimelinesViewModel::class.java) }
    private val ui = TimelinesUI()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ui.createView(AnkoContext.create(context!!, this))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.run {
            columns().observeOnNotNull(this@TimelinesFragment) { columns ->
                ui.timelines.adapter = TimelineAdapter(fragmentManager, columns)
            }
        }
    }
}
