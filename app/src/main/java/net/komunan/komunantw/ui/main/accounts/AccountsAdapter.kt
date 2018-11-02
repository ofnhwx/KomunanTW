package net.komunan.komunantw.ui.main.accounts

import android.net.Uri
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_account.view.*
import net.komunan.komunantw.R
import net.komunan.komunantw.repository.entity.Account
import net.komunan.komunantw.ui.common.SimpleListAdapter

class AccountsAdapter(accounts: List<Account>): SimpleListAdapter<Account>(accounts) {
    override fun newView(position: Int, parent: ViewGroup): View {
        return inflater.inflate(R.layout.item_account, parent, false)
    }

    override fun bindView(view: View, position: Int) {
        val account = getItem(position)
        view.account_icon.setImageURI(Uri.parse(account.imageUrl))
        view.account_name.text = account.name
        view.account_screen_name.text = account.screenName
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id
    }
}
