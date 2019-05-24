package net.komunan.komunantw.ui.source.list

import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.komunan.komunantw.R
import net.komunan.komunantw.core.repository.entity.Source
import net.komunan.komunantw.databinding.ItemSourceBinding
import net.komunan.komunantw.ui.common.base.TWBaseListAdapter

class SourceListAdapter : TWBaseListAdapter<Source, SourceListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(inflate(R.layout.item_source, parent))
    }

    class ViewHolder(itemView: View) : TWBaseListAdapter.ViewHolder<Source>(itemView) {
        private val binding by lazy { DataBindingUtil.bind<ItemSourceBinding>(itemView)!! }

        override fun bind(item: Source) {
            GlobalScope.launch(Dispatchers.Main) {
                binding.account = withContext(Dispatchers.Default) { item.account.target }
            }
            binding.source = item
        }
    }
}
