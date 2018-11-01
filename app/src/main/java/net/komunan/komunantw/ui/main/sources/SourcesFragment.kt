package net.komunan.komunantw.ui.main.sources

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.komunan.komunantw.databinding.SimpleListViewBinding
import net.komunan.komunantw.observeOnNotNull

class SourcesFragment: Fragment() {
    companion object {
        @JvmStatic
        fun create(): Fragment = SourcesFragment()
    }

    private lateinit var binding: SimpleListViewBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return SimpleListViewBinding.inflate(inflater, container, false).apply { binding = this }.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        super.onActivityCreated(savedInstanceState)
        val viewModel = ViewModelProviders.of(this).get(SourcesViewModel::class.java)
        viewModel.sources.observeOnNotNull(this) { sources ->
            binding.container.adapter = SourcesAdapter(sources)
        }
    }
}
