package net.komunan.komunantw.repository.entity

import android.arch.persistence.room.*

@Entity(
        primaryKeys = ["id", "source_id"],
        foreignKeys = [
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
class Tweet {
    var id: Long = 0
    @ColumnInfo(name = "source_id", index = true)
    var sourceId: Long = 0
}
