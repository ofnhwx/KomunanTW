package net.komunan.komunantw.ui.accounts

import android.view.View
import android.view.ViewGroup
import net.komunan.komunantw.ReleaseApplication
import net.komunan.komunantw.common.SimpleListAdapter
import net.komunan.komunantw.repository.entity.Account
import org.jetbrains.anko.UI

internal class AccountsAdapter internal constructor(accounts: List<Account>): SimpleListAdapter<Account>(accounts) {
    override fun getItemId(position: Int): Long {
        return items[position].id
    }

    override fun newView(position: Int, parent: ViewGroup): View {
        AccountUI().let { ui ->
            return ui.createView(ReleaseApplication.context.UI {  }).also { view ->
                view.tag = ui
            }
        }
    }

    override fun bindView(view: View, position: Int) {
        (view.tag as AccountUI).update(items[position])
    }
}
