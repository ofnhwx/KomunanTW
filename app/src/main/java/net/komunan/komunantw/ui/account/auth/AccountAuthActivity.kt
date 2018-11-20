package net.komunan.komunantw.ui.account.auth

import android.content.ComponentName
import android.content.Intent
import androidx.fragment.app.Fragment
import net.komunan.komunantw.TWContext
import net.komunan.komunantw.ui.common.base.TWBaseActivity

class AccountAuthActivity: TWBaseActivity() {
    companion object {
        private const val PARAMETER_FIRST_RUN = "AccountAuthActivity.PARAMETER_FIRST_RUN"

        @JvmStatic
        fun createIntent(firstRun: Boolean = false): Intent {
            val componentName = ComponentName(TWContext, AccountAuthActivity::class.java)
            val intent = if (firstRun) Intent.makeRestartActivityTask(componentName) else Intent.makeMainActivity(componentName)
            return intent.apply {
                putExtra(PARAMETER_FIRST_RUN, firstRun)
            }
        }
    }

    override fun content(): Fragment? {
        val firstRun = intent.getBooleanExtra(PARAMETER_FIRST_RUN, false)
        return AccountAuthFragment.create(firstRun)
    }
}
