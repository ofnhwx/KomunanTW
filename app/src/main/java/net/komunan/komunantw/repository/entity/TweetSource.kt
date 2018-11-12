package net.komunan.komunantw.repository.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import org.apache.commons.lang3.builder.ToStringBuilder

@Entity(
        tableName = "tweet_source",
        primaryKeys = ["tweet_id", "source_id", "is_missing"],
        foreignKeys = [
            ForeignKey(
                    entity = Tweet::class,
                    parentColumns = ["id"],
                    childColumns = ["tweet_id"],
                    onDelete = ForeignKey.CASCADE,
                    onUpdate = ForeignKey.CASCADE,
                    deferred = true
            )
        ]
)
open class TweetSource() {
    @ColumnInfo(name = "tweet_id")   var tweetId   : Long = 0L
    @ColumnInfo(name = "source_id")  var sourceId  : Long = 0L
    @ColumnInfo(name = "is_missing") var isMissing : Boolean = false

    @Ignore
    constructor(sourceId: Long, tweetId: Long): this() {
        this.sourceId = sourceId
        this.tweetId  = tweetId
    }

    @Ignore
    constructor(sourceId: Long, tweetId: Long, isMissing: Boolean): this() {
        this.sourceId  = sourceId
        this.tweetId   = tweetId
        this.isMissing = isMissing
    }

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }
}
