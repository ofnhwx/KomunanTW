package net.komunan.komunantw.ui.accounts

import android.content.Context
import android.widget.TextView
import net.komunan.komunantw.repository.entity.Account
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout


internal class AccountUI: AnkoComponent<Context> {
    private lateinit var name: TextView

    override fun createView(ui: AnkoContext<Context>) = with(ui) {
        verticalLayout {
            name = textView()
        }
    }

    fun update(account: Account) {
        name.text = account.name
    }
}
