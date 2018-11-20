package net.komunan.komunantw.ui.source.list

import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_source.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.komunan.komunantw.R
import net.komunan.komunantw.repository.entity.Account
import net.komunan.komunantw.repository.entity.Source
import net.komunan.komunantw.ui.common.base.TWBaseListAdapter

class SourceListAdapter: TWBaseListAdapter<Source, SourceListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(inflate(R.layout.item_source, parent))
    }

    class ViewHolder(itemView: View): TWBaseListAdapter.ViewHolder<Source>(itemView) {
        override fun bind(item: Source) {
            GlobalScope.launch(Dispatchers.Main) {
                val account = withContext(Dispatchers.Default) { Account.dao.find(item.accountId)!! }
                itemView.source_account_icon.setImageURI(account.imageUrl)
                itemView.source_account_name.text = account.name
                itemView.source_name.text =item.displayName
            }
        }
    }
}
