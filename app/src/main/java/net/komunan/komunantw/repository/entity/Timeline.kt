package net.komunan.komunantw.repository.entity

import androidx.room.*
import net.komunan.komunantw.repository.database.TWDatabase

@Entity(tableName = "timeline")
data class Timeline(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        var id: Long,
        @ColumnInfo(name = "name")
        var name: String,
        @ColumnInfo(name = "position")
        var position: Int,
        @ColumnInfo(name = "create_at")
        var createAt: Long,
        @ColumnInfo(name = "update_at")
        var updateAt: Long
) {
    companion object {
        private val dao = TWDatabase.instance.timelineDao()
        private val sourceDao = TWDatabase.instance.timelineSourceDao()

        @JvmStatic fun findAllAsync() = dao.findAllAsync()
        @JvmStatic fun findAsync(id: Long) = dao.findAsync(id)
        @JvmStatic fun find(id: Long) = dao.find(id)
        @JvmStatic fun count() = dao.count()
        @JvmStatic fun firstSetup(account: Account) = dao.firstSetup(account)
    }

    @Ignore
    constructor(name: String): this(
            id = 0,
            name = name,
            position = 0,
            createAt = 0,
            updateAt = 0
    )

    fun save() = dao.save(this)
    fun delete() = dao.delete(this)
    fun moveTo(position: Int) = dao.moveTo(this, position)
    fun addSource(source: Source) = sourceDao.add(this, source)
    fun removeSource(source: Source) = sourceDao.remove(this, source)
    fun sources() = Source.findByTimeline(this)
    fun sourceCount() = Source.countByTimeline(this)
}

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
        ]
)
data class TimelineSource(
        @ColumnInfo(name = "timeline_id")
        var timelineId: Long,
        @ColumnInfo(name = "source_id", index = true)
        var sourceId: Long
)
