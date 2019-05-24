package net.komunan.komunantw.ui.timeline.list

import android.content.Intent
import androidx.fragment.app.Fragment
import net.komunan.komunantw.TWContext
import net.komunan.komunantw.ui.common.base.TWBaseActivity

class TimelineListActivity : TWBaseActivity() {
    companion object {
        @JvmStatic
        fun createIntent(): Intent {
            return Intent(TWContext, TimelineListActivity::class.java)
        }
    }

    override val content: Fragment?
        get() = TimelineListFragment.create()
}
