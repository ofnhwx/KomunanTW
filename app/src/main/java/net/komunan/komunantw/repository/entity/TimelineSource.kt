package net.komunan.komunantw.repository.entity

import androidx.room.*
import org.apache.commons.lang3.builder.ToStringBuilder

@Entity(
        tableName = "timeline_source",
        primaryKeys = ["timeline_id", "source_id"],
        foreignKeys = [
            ForeignKey(
                    entity = Timeline::class,
                    parentColumns = ["id"],
                    childColumns = ["timeline_id"],
                    onDelete = ForeignKey.CASCADE,
                    onUpdate = ForeignKey.CASCADE,
                    deferred = true
            ),
            ForeignKey(
                    entity = Source::class,
                    parentColumns = ["id"],
                    childColumns = ["source_id"],
                    onDelete = ForeignKey.CASCADE,
                    onUpdate = ForeignKey.CASCADE,
                    deferred = true
            )
        ],
        indices = [ Index("source_id") ]
)
class TimelineSource() {
    @ColumnInfo(name = "timeline_id") var timelineId: Long = 0L
    @ColumnInfo(name = "source_id")   var sourceId  : Long = 0L

    @Ignore
    constructor(timelineId: Long, sourceId: Long): this() {
        this.timelineId = timelineId
        this.sourceId   = sourceId
    }

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }

    fun save() = Timeline.sourceDao.save(this)
    fun delete() = Timeline.sourceDao.delete(this)
}
