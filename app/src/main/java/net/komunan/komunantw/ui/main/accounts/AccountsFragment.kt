package net.komunan.komunantw.ui.main.accounts

import android.os.Bundle
import android.view.*
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.simple_recycler_view.*
import net.komunan.komunantw.R
import net.komunan.komunantw.event.Transition
import net.komunan.komunantw.observeOnNotNull
import net.komunan.komunantw.ui.common.TWBaseFragment

class AccountsFragment: TWBaseFragment() {
    companion object {
        @JvmStatic
        fun create() = AccountsFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.simple_recycler_view, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val viewModel = ViewModelProviders.of(this).get(AccountsViewModel::class.java)
        viewModel.accounts.observeOnNotNull(this) { accounts ->
            container.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            container.adapter = AccountsAdapter(accounts)
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
