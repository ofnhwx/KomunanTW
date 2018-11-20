package net.komunan.komunantw.ui.account.list

import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_account.view.*
import net.komunan.komunantw.R
import net.komunan.komunantw.repository.entity.Account
import net.komunan.komunantw.ui.common.base.TWBaseListAdapter

class AccountListAdapter: TWBaseListAdapter<Account, AccountListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(inflate(R.layout.item_account, parent))
    }

    class ViewHolder(itemView: View): TWBaseListAdapter.ViewHolder<Account>(itemView) {
        override fun bind(item: Account) {
            itemView.account_icon.setImageURI(item.imageUrl)
            itemView.account_name.text = item.name
            itemView.account_screen_name.text = item.screenName
        }
    }
}
