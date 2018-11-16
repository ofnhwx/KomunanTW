package net.komunan.komunantw.ui.main.accounts

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_account.view.*
import net.komunan.komunantw.R
import net.komunan.komunantw.repository.entity.Account
import net.komunan.komunantw.ui.common.base.TWListAdapter

class AccountsAdapter: TWListAdapter<Account, AccountsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(inflate(R.layout.item_account, parent))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(account: Account) {
            itemView.account_icon.setImageURI(account.imageUrl)
            itemView.account_name.text = account.name
            itemView.account_screen_name.text = account.screenName
        }
    }
}
