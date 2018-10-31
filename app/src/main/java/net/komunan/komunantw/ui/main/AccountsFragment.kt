package net.komunan.komunantw.ui.main

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModelProviders
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import net.komunan.komunantw.R
import net.komunan.komunantw.draweeView
import net.komunan.komunantw.event.Transition
import net.komunan.komunantw.observeOnNotNull
import net.komunan.komunantw.repository.entity.Account
import net.komunan.komunantw.string
import net.komunan.komunantw.ui.common.SimpleListAdapter
import net.komunan.komunantw.ui.common.TWBaseViewModel
import org.jetbrains.anko.*

class AccountsFragment: Fragment() {
    companion object {
        @JvmStatic
        fun create() = AccountsFragment()
    }

    private val viewModel by lazy { ViewModelProviders.of(this).get(AccountsViewModel::class.java) }
    private val ui = AccountsUI()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ui.createView(AnkoContext.create(context!!, this))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.run {
            accounts.observeOnNotNull(this@AccountsFragment) { accounts ->
                ui.accounts.adapter = AccountsAdapter(accounts)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        if (menu == null) {
            return
        }
        menu.add(0, R.string.add_account, 1, R.string.add_account)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.string.add_account -> {
                Transition.execute(Transition.Target.AUTH)
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }
}

class AccountsViewModel: TWBaseViewModel() {
    val accounts: LiveData<List<Account>>
        get() = Account.findAllAsync()
}

private class AccountsAdapter internal constructor(accounts: List<Account>): SimpleListAdapter<Account>(accounts) {
    override fun newView(position: Int, parent: ViewGroup): View {
        AccountUI().let { ui ->
            return ui.createView(AnkoContext.create(parent.context, parent)).also { view ->
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

private class AccountsUI: AnkoComponent<AccountsFragment> {
    lateinit var accounts: ListView

    override fun createView(ui: AnkoContext<AccountsFragment>): View = with(ui) {
        accounts = listView {}
        return@with accounts
    }
}

private class AccountUI: AnkoComponent<ViewGroup> {
    lateinit var userIcon: ImageView
    lateinit var userName: TextView
    lateinit var screenName: TextView

    override fun createView(ui: AnkoContext<ViewGroup>): View = with(ui) {
        linearLayout {
            userIcon = draweeView {
                id = R.id.account_icon
            }.lparams(dip(48), dip(48))
            verticalLayout {
                userName = textView {
                    id = R.id.account_name
                }
                screenName = textView {
                    id = R.id.account_screen_name
                }
            }
        }
    }

    fun bind(account: Account) {
        userIcon.setImageURI(Uri.parse(account.imageUrl))
        userName.text = account.name
        screenName.text = R.string.format_screen_name.string(account.screenName)
    }
}
