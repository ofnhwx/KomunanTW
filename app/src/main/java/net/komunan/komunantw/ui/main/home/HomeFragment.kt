package net.komunan.komunantw.ui.main.home

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.komunan.komunantw.databinding.SimpleViewPagerBinding
import net.komunan.komunantw.observeOnNotNull

class HomeFragment: Fragment() {
    companion object {
        @JvmStatic
        fun create() = HomeFragment()
    }

    private lateinit var binding: SimpleViewPagerBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return SimpleViewPagerBinding.inflate(inflater, container, false).apply { binding = this }.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val viewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        viewModel.timelines.observeOnNotNull(this) { timelines ->
            binding.container.adapter = HomeAdapter(fragmentManager, timelines)
        }
    }
}
