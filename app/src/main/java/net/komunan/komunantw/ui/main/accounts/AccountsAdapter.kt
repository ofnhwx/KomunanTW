package net.komunan.komunantw.ui.main.accounts

import android.databinding.DataBindingUtil
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import net.komunan.komunantw.databinding.ItemAccountBinding
import net.komunan.komunantw.repository.entity.Account
import net.komunan.komunantw.ui.common.SimpleListAdapter

class AccountsAdapter(accounts: List<Account>): SimpleListAdapter<Account>(accounts) {
    override fun newView(position: Int, parent: ViewGroup): View {
        return ItemAccountBinding.inflate(inflater, parent, false).root
    }

    override fun bindView(view: View, position: Int) {
        DataBindingUtil.bind<ItemAccountBinding>(view)?.let {
            val account = getItem(position)
            it.accountIcon.setImageURI(Uri.parse(account.imageUrl))
            it.accountName.text = account.name
            it.accountScreenName.text = account.screenName
        }
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id
    }
}
