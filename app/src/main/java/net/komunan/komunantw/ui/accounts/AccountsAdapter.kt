package net.komunan.komunantw.ui.accounts

import android.view.View
import android.view.ViewGroup
import net.komunan.komunantw.ReleaseApplication
import net.komunan.komunantw.common.SimpleListAdapter
import net.komunan.komunantw.repository.entity.Account
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.UI

internal class AccountsAdapter internal constructor(accounts: List<Account>): SimpleListAdapter<Account>(accounts) {
    override fun newView(position: Int, parent: ViewGroup): View {
        AccountUI().let { ui ->
            return ui.createView(AnkoContext.Companion.create(parent.context, parent)).also { view ->
                view.tag = ui
            }
        }
    }

    override fun bindView(view: View, position: Int) {
        (view.tag as AccountUI).bind(items[position])
    }

    override fun getItemId(position: Int): Long {
        return items[position].id
    }
}
