package net.komunan.komunantw.repository.entity

import android.arch.persistence.room.*
import net.komunan.komunantw.repository.database.TWDatabase

@Entity(
        primaryKeys = ["column_id", "source_id"],
        foreignKeys = [
            ForeignKey(
                    entity = Column::class,
                    parentColumns = ["id"],
                    childColumns = ["column_id"],
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
class ColumnSource {
    @ColumnInfo(name = "column_id")
    var columnId: Long = 0

    @ColumnInfo(name = "source_id", index = true)
    var sourceId: Long = 0

    fun save() = TWDatabase.instance.columnSourceDao().save(this)
    fun delete() = TWDatabase.instance.columnSourceDao().delete(this)
}
