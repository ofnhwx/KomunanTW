package net.komunan.komunantw.ui.timeline.edit

import android.content.Intent
import androidx.fragment.app.Fragment
import net.komunan.komunantw.TWContext
import net.komunan.komunantw.ui.common.base.TWBaseActivity

class TimelineEditActivity: TWBaseActivity() {
    companion object {
        private const val PARAMETER_TIMELINE_ID = "TimelineEditActivity.PARAMETER_TIMELINE_ID"

        @JvmStatic
        fun createIntent(timelineId: Long): Intent {
            return Intent(TWContext, TimelineEditActivity::class.java).apply {
                putExtra(PARAMETER_TIMELINE_ID, timelineId)
            }
        }
    }

    override val content: Fragment?
        get() {
            val timelineId = intent.getLongExtra(PARAMETER_TIMELINE_ID, 0)
            return TimelineEditFragment.create(timelineId)
        }
}
