package net.komunan.komunantw.ui.account.list

import android.content.Intent
import androidx.fragment.app.Fragment
import net.komunan.komunantw.TWContext
import net.komunan.komunantw.ui.common.base.TWBaseActivity

class AccountListActivity : TWBaseActivity() {
    companion object {
        @JvmStatic
        fun createIntent(): Intent {
            return Intent(TWContext, AccountListActivity::class.java)
        }
    }

    override val content: Fragment?
        get() = AccountListFragment.create()
}
