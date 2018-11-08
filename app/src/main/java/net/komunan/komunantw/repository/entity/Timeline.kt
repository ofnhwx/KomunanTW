package net.komunan.komunantw.repository.entity

import androidx.room.*
import net.komunan.komunantw.common.Diffable
import net.komunan.komunantw.repository.database.TWDatabase

@Entity(tableName = "timeline")
class Timeline(): Diffable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")        var id      : Long = 0L
    @ColumnInfo(name = "name")      var name    : String = ""
    @ColumnInfo(name = "position")  var position: Int = 0
    @ColumnInfo(name = "create_at") var createAt: Long = 0L
    @ColumnInfo(name = "update_at") var updateAt: Long = 0L

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
    constructor(name: String): this() {
        this.name = name
    }

    fun save() = dao.save(this)
    fun delete() = dao.delete(this)
    fun moveTo(position: Int) = dao.moveTo(this, position)
    fun addSource(source: Source) = sourceDao.add(this, source)
    fun removeSource(source: Source) = sourceDao.remove(this, source)
    fun sources() = Source.findByTimeline(this)
    fun sourceCount() = Source.countByTimeline(this)

    override fun toString(): String {
        return "${Timeline::class.simpleName}{ " +
                "id=$id," +
                "name=$name, " +
                "position=$position, " +
                "createAt=$createAt, " +
                "updateAt=$updateAt }"
    }

    override fun isTheSame(other: Diffable): Boolean {
        return other is Timeline
                && this.id == other.id
    }

    override fun isContentsTheSame(other: Diffable): Boolean {
        return other is Timeline
                && this.id == other.id
                && this.name == other.name
                //&& this.position == other.position
    }
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
        ],
        indices = [ Index("source_id") ]
)
class TimelineSource() {
    @ColumnInfo(name = "timeline_id") var timelineId: Long = 0L
    @ColumnInfo(name = "source_id")   var sourceId  : Long = 0L

    @Ignore
    constructor(timeline: Timeline, source: Source): this() {
        this.timelineId = timeline.id
        this.sourceId = source.id
    }
}
