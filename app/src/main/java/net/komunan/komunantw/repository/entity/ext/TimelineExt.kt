package net.komunan.komunantw.repository.entity.ext

import androidx.room.ColumnInfo
import net.komunan.komunantw.R
import net.komunan.komunantw.common.extension.string
import net.komunan.komunantw.repository.entity.Timeline
import net.komunan.komunantw.ui.common.base.Diffable

class TimelineExt: Timeline() {
    @ColumnInfo(name = "source_count") var sourceCount: Long = 0

    fun displaySourceCount(): String {
        return string[R.string.fragment_timeline_list_source_count](sourceCount.toString())
    }

    override fun isContentsTheSame(other: Diffable): Boolean {
        return super.isContentsTheSame(other)
                && other is TimelineExt
                && this.sourceCount == other.sourceCount
    }
}
