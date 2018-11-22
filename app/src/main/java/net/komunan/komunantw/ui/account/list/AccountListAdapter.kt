package net.komunan.komunantw.ui.account.list

import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import net.komunan.komunantw.R
import net.komunan.komunantw.databinding.ItemAccountBinding
import net.komunan.komunantw.repository.entity.Account
import net.komunan.komunantw.ui.common.base.TWBaseListAdapter

class AccountListAdapter: TWBaseListAdapter<Account, AccountListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(inflate(R.layout.item_account, parent))
    }

    class ViewHolder(itemView: View): TWBaseListAdapter.ViewHolder<Account>(itemView) {
        private val binding by lazy { DataBindingUtil.bind<ItemAccountBinding>(itemView)!! }

        override fun bind(item: Account) {
            binding.account = item
        }
    }
}
