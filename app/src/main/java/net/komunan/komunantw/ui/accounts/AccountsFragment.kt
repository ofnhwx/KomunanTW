package net.komunan.komunantw.ui.accounts

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import net.komunan.komunantw.R
import net.komunan.komunantw.common.observeOnNotNull
import net.komunan.komunantw.event.Transition
import org.jetbrains.anko.AnkoContext

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
            accounts().observeOnNotNull(this@AccountsFragment) { accounts -> ui.bind(accounts) }
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
            else -> super.onOptionsItemSelected(item)
        }
    }
}
