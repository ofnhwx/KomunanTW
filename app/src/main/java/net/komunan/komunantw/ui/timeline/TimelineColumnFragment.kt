package net.komunan.komunantw.ui.timeline

import android.support.v4.app.Fragment
import net.komunan.komunantw.repository.entity.Column

class TimelineColumnFragment: Fragment() {
    companion object {
        @JvmStatic
        fun create(column: Column) = TimelineColumnFragment()
    }
}
