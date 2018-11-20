package net.komunan.komunantw.ui.source.list

import android.content.Intent
import androidx.fragment.app.Fragment
import net.komunan.komunantw.TWContext
import net.komunan.komunantw.ui.common.base.TWBaseActivity

class SourceListActivity: TWBaseActivity() {
    companion object {
        @JvmStatic
        fun createIntent(): Intent {
            return Intent(TWContext, SourceListActivity::class.java)
        }
    }

    override fun content(): Fragment? {
        return SourceListFragment.create()
    }
}
