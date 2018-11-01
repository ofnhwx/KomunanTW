package net.komunan.komunantw.ui.main.sources

import android.databinding.DataBindingUtil
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.*
import net.komunan.komunantw.R
import net.komunan.komunantw.databinding.ItemSourceBinding
import net.komunan.komunantw.repository.entity.Source
import net.komunan.komunantw.string
import net.komunan.komunantw.ui.common.SimpleListAdapter

class SourcesAdapter(sources: List<Source>): SimpleListAdapter<Source>(sources) {
    override fun newView(position: Int, parent: ViewGroup): View {
        return ItemSourceBinding.inflate(inflater, parent, false).root
    }

    override fun bindView(view: View, position: Int) {
        DataBindingUtil.bind<ItemSourceBinding>(view)?.let {
            GlobalScope.launch(Dispatchers.Main) {
                val source = getItem(position)
                val account = GlobalScope.async { source.account() }.await()
                it.accountName.text = account?.name
                it.sourceName.text = when (Source.SourceType.valueOf(source.type)) {
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

    override fun getItemId(position: Int): Long {
        return getItem(position).id
    }
}
