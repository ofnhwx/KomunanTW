package net.komunan.komunantw.repository.entity

import androidx.room.*
import org.apache.commons.lang3.builder.ToStringBuilder
import twitter4j.Status

@Entity(
        tableName = "tweet_account",
        primaryKeys = ["tweet_id", "account_id"],
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
class TweetAccount() {
    @ColumnInfo(name = "tweet_id")   var tweetId  : Long = 0L
    @ColumnInfo(name = "account_id") var accountId: Long = 0L
    @ColumnInfo(name = "is_missing") var isMissing   : Boolean = false
    @ColumnInfo(name = "retweeted")  var retweeted: Boolean = false
    @ColumnInfo(name = "liked")      var liked    : Boolean = false

    @Ignore
    constructor(accountId: Long, status: Status): this() {
        this.tweetId   = status.id
        this.accountId = accountId
        this.retweeted = status.isRetweeted
        this.liked     = status.isFavorited
    }

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }

    fun save() = Tweet.accountDao.save(this)
}