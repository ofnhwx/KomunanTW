package net.komunan.komunantw.ui.account.list

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.komunan.komunantw.R
import net.komunan.komunantw.common.extension.string
import net.komunan.komunantw.repository.entity.Account
import net.komunan.komunantw.ui.account.auth.AccountAuthActivity
import net.komunan.komunantw.ui.common.base.TWBaseListAdapter
import net.komunan.komunantw.ui.common.base.TWBaseListFragment

class AccountListFragment: TWBaseListFragment<Account, AccountListAdapter.ViewHolder>() {
    companion object {
        @JvmStatic
        fun create() = AccountListFragment()
    }

    override val name: String?
        get() = string[R.string.fragment_account_list]()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        if (menu == null) {
            return
        }
        menu.add(0, R.string.add, 1, R.string.add).apply {
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.string.add -> startActivity(AccountAuthActivity.createIntent())
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun adapter(): TWBaseListAdapter<Account, AccountListAdapter.ViewHolder> {
        return AccountListAdapter()
    }

    override fun layoutManager(): RecyclerView.LayoutManager {
        return LinearLayoutManager(context, RecyclerView.VERTICAL, false)
    }

    override fun items(): LiveData<List<Account>> {
        return viewModel(AccountListViewModel::class.java).accounts
    }
}
