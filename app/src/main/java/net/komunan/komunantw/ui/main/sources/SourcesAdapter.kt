package net.komunan.komunantw.ui.main.sources

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_source.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.komunan.komunantw.R
import net.komunan.komunantw.repository.entity.Account
import net.komunan.komunantw.repository.entity.Source
import net.komunan.komunantw.ui.common.base.TWListAdapter

class SourcesAdapter: TWListAdapter<Source, SourcesAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(inflate(R.layout.item_source, parent))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(source: Source) = GlobalScope.launch(Dispatchers.Main) {
            val account = withContext(Dispatchers.Default) { Account.dao.find(source.accountId)!! }
            itemView.source_account_icon.setImageURI(account.imageUrl)
            itemView.source_account_name.text = account.name
            itemView.source_name.text =source.displayName
        }
    }
}
