package net.komunan.komunantw.ui.accounts

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ui.createView(AnkoContext.create(context!!, this))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.run {
            accounts().observeOnNotNull(this@AccountsFragment) { accounts ->
                ui.accounts.adapter = AccountsAdapter(accounts)
            }
        }
        ui.run {
            add.setOnClickListener { _ -> Transition.execute(Transition.Target.AUTH) }
        }
    }
}
