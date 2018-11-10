package net.komunan.komunantw.repository.entity

import androidx.room.*
import net.komunan.komunantw.common.Diffable
import org.apache.commons.lang3.builder.ToStringBuilder

@Entity(
        tableName = "tweet_source",
        primaryKeys = ["tweet_id", "source_id", "is_missing"],
        foreignKeys = [
            ForeignKey(
                    entity = Tweet::class,
                    parentColumns = ["id", "is_missing"],
                    childColumns = ["tweet_id", "is_missing"],
                    onDelete = ForeignKey.CASCADE,
                    onUpdate = ForeignKey.CASCADE,
                    deferred = true
            )
        ],
        indices = [Index("tweet_id", "is_missing")]
)
class TweetSource(): Diffable {
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

    override fun isTheSame(other: Diffable): Boolean {
        return other is TweetSource
                && this.tweetId == other.tweetId
    }

    override fun isContentsTheSame(other: Diffable): Boolean {
        return false
    }
}
