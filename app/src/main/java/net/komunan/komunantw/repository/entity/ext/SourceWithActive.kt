package net.komunan.komunantw.repository.entity.ext

import androidx.room.ColumnInfo
import net.komunan.komunantw.ui.common.base.Diffable
import net.komunan.komunantw.repository.entity.Source

class SourceWithActive: Source() {
    @ColumnInfo(name = "is_active") var isActive: Boolean = false

    override fun isContentsTheSame(other: Diffable): Boolean {
        return super.isContentsTheSame(other)
                && other is SourceWithActive
                && this.isActive == other.isActive
    }
}
