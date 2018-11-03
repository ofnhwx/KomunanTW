package net.komunan.komunantw.ui.main.timelines

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.simple_recycler_view.*
import net.komunan.komunantw.R
import net.komunan.komunantw.observeOnNotNull
import net.komunan.komunantw.ui.common.TWBaseFragment

class TimelinesFragment: TWBaseFragment() {
    companion object {
        @JvmStatic
        fun create() = TimelinesFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.simple_recycler_view, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val viewModel = ViewModelProviders.of(this).get(TimelinesViewModel::class.java)
        viewModel.timelines.observeOnNotNull(this) { timelines ->
            container.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            container.adapter = TimelinesAdapter(timelines)
        }
    }
}
