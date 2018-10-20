package net.komunan.komunantw.ui.accounts

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.constraint.ConstraintSet.PARENT_ID
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import net.komunan.komunantw.ReleaseApplication
import net.komunan.komunantw.common.SimpleListAdapter
import net.komunan.komunantw.event.Transition
import net.komunan.komunantw.repository.entity.Account
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.design.floatingActionButton

class AccountsFragment: Fragment() {
    companion object {
        @JvmStatic
        fun create() = AccountsFragment()
    }

    private val viewModel by lazy { ViewModelProviders.of(this).get(AccountsViewModel::class.java) }
    private val ui = AccountsUI()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ui.createView(AnkoContext.create(context!!, this))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.accounts.observe(this, Observer { accounts ->
            accounts?.let {
                ui.accounts.adapter = AccountsAdapter(it)
            }
        })
        ui.add.setOnClickListener { _ -> Transition.execute(Transition.Target.AUTH) }
    }

    private class AccountsUI: AnkoComponent<AccountsFragment> {
        lateinit var accounts: ListView
        lateinit var add: FloatingActionButton

        override fun createView(ui: AnkoContext<AccountsFragment>) = with(ui) {
            constraintLayout {
                listView {
                    accounts = this
                }.lparams(0, 0) {
                    topToTop = PARENT_ID
                    startToStart = PARENT_ID
                    endToEnd = PARENT_ID
                    bottomToBottom = PARENT_ID
                }

                floatingActionButton {
                    add = this
                }.lparams {
                    endToEnd = PARENT_ID
                    bottomToBottom = PARENT_ID
                    rightMargin = dip(16.0f)
                    bottomMargin = dip(16.0f)
                }
            }
        }
    }

    private class AccountUI: AnkoComponent<Context> {
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

    private class AccountsAdapter<out T: Account> internal constructor(accounts: List<T>): SimpleListAdapter<T>(accounts) {
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
}
