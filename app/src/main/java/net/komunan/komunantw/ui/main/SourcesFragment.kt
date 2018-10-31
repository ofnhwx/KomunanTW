package net.komunan.komunantw.ui.main

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import net.komunan.komunantw.R
import net.komunan.komunantw.observeOnNotNull
import net.komunan.komunantw.repository.entity.Source
import net.komunan.komunantw.string
import net.komunan.komunantw.ui.common.SimpleListAdapter
import net.komunan.komunantw.ui.common.TWBaseViewModel
import org.jetbrains.anko.*

class SourcesFragment: Fragment() {
    companion object {
        @JvmStatic
        fun create(): Fragment = SourcesFragment()
    }

    private val viewModel by lazy { ViewModelProviders.of(this).get(SourcesViewModel::class.java) }
    private val ui = SourcesUI()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ui.createView(AnkoContext.create(context!!, this))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.run {
            sources.observeOnNotNull(this@SourcesFragment) { sources ->
                ui.sources.adapter = SourcesAdapter(sources)
            }
        }
    }
}

class SourcesViewModel: TWBaseViewModel() {
    val sources: LiveData<List<Source>>
        get() = Source.findAllAsync()
}

private class SourcesAdapter internal constructor(sources: List<Source>): SimpleListAdapter<Source>(sources) {
    override fun newView(position: Int, parent: ViewGroup): View {
        val ui = SourceUI()
        return ui.createView(AnkoContext.create(parent.context, parent)).apply {
            tag = ui
        }
    }

    override fun bindView(view: View, position: Int) {
        (view.tag as SourceUI).bind(items[position])
    }

    override fun getItemId(position: Int): Long {
        return items[position].id
    }
}

private class SourcesUI: AnkoComponent<SourcesFragment> {
    lateinit var sources: ListView

    override fun createView(ui: AnkoContext<SourcesFragment>): View = with(ui) {
        sources = listView {}
        return@with sources
    }
}

private class SourceUI: AnkoComponent<ViewGroup> {
    lateinit var account: TextView
    lateinit var name: TextView

    override fun createView(ui: AnkoContext<ViewGroup>): View = with(ui) {
        verticalLayout {
            account = textView().lparams(matchParent, wrapContent)
            name = textView().lparams(matchParent, wrapContent)
        }
    }

    fun bind(source: Source) {
        launch(UI) {
            val account = withContext(CommonPool) { source.account() }
            this@SourceUI.account.text = account?.name
            this@SourceUI.name.text = when (Source.SourceType.valueOf(source.type)) {
                Source.SourceType.HOME -> R.string.home.string()
                Source.SourceType.MENTION -> R.string.mention.string()
                Source.SourceType.RETWEET -> R.string.retweet.string()
                Source.SourceType.USER -> R.string.user.string()
                Source.SourceType.LIST -> R.string.format_list_label.string(source.label)
                Source.SourceType.SEARCH -> R.string.format_search_label.string(source.label)
            }
        }
    }
}
