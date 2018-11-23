package net.komunan.komunantw.repository.entity

import androidx.room.*
import net.komunan.komunantw.ui.common.base.Diffable
import net.komunan.komunantw.repository.database.TWDatabase
import org.apache.commons.lang3.builder.ToStringBuilder

@Entity(tableName = "timeline")
open class Timeline(): Diffable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")        var id      : Long = 0L
    @ColumnInfo(name = "name")      var name    : String = ""
    @ColumnInfo(name = "position")  var position: Int = 0
    @ColumnInfo(name = "create_at") var createAt: Long = 0L
    @ColumnInfo(name = "update_at") var updateAt: Long = 0L

    companion object {
        @JvmStatic
        val dao = TWDatabase.instance.timelineDao()
        @JvmStatic
        val sourceDao = TWDatabase.instance.timelineSourceDao()
    }

    @Ignore
    constructor(name: String): this() {
        this.name = name
    }

    fun save() = dao.save(this)
    fun delete() = dao.delete(this)
    fun moveTo(position: Int) = dao.moveTo(this, position)

    fun addSource(source: Source): Unit = TimelineSource(id, source.id).save()
    fun delSource(source: Source): Unit = TimelineSource(id, source.id).delete()

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }

    override fun isTheSame(other: Diffable): Boolean {
        return other is Timeline
                && this.id == other.id
    }

    override fun isContentsTheSame(other: Diffable): Boolean {
        return other is Timeline
                && this.id == other.id
                && this.name == other.name
    }
}
