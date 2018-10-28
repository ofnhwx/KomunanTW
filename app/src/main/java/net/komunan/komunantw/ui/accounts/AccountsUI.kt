package net.komunan.komunantw.ui.accounts

import android.widget.ListView
import net.komunan.komunantw.repository.entity.Account
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.listView

internal class AccountsUI: AnkoComponent<AccountsFragment> {
    lateinit var accounts: ListView

    override fun createView(ui: AnkoContext<AccountsFragment>) = with(ui) {
        listView {
            accounts = this
        }
    }

    fun bind(accounts: List<Account>) {
        this.accounts.adapter = AccountsAdapter(accounts)
    }
}
