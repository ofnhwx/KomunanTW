package net.komunan.komunantw.ui.main.sources

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.simple_list_view.*
import net.komunan.komunantw.R
import net.komunan.komunantw.observeOnNotNull

class SourcesFragment: Fragment() {
    companion object {
        @JvmStatic
        fun create(): Fragment = SourcesFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.simple_list_view, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        super.onActivityCreated(savedInstanceState)
        val viewModel = ViewModelProviders.of(this).get(SourcesViewModel::class.java)
        viewModel.sources.observeOnNotNull(this) { sources ->
            container.adapter = SourcesAdapter(sources)
        }
    }
}
