package net.komunan.komunantw.repository.entity

import android.arch.persistence.room.*
import net.komunan.komunantw.repository.database.TWDatabase
import net.komunan.komunantw.repository.database.transaction
import net.komunan.komunantw.service.TwitterService

@Entity(
        tableName = "source",
        foreignKeys = [
            ForeignKey(
                    entity = Account::class,
                    parentColumns = ["id"],
                    childColumns = ["account_id"],
                    onDelete = ForeignKey.CASCADE,
                    onUpdate = ForeignKey.CASCADE,
                    deferred = true
            )
        ]
)
data class Source(
        @PrimaryKey(autoGenerate = true)
        var id: Long,
        @ColumnInfo(name = "account_id", index = true)
        var accountId: Long,
        @ColumnInfo(name = "order")
        var order: Int,
        @ColumnInfo(name = "type")
        var type: String,
        @ColumnInfo(name = "label")
        var label: String,
        @ColumnInfo(name = "query")
        var query: String?,
        @ColumnInfo(name = "list_owner")
        var listOwner: Long,
        @ColumnInfo(name = "list_id")
        var listId: Long,
        @ColumnInfo(name = "fetch_at")
        var fetchAt: Long,
        @ColumnInfo(name = "create_at")
        var createAt: Long,
        @ColumnInfo(name = "update_at")
        var updateAt: Long
) {
    enum class SourceType {
        HOME(),
        MENTION(),
        RETWEET(),
        USER(),
        LIST(),
        SEARCH(),
    }

    companion object {
        private val dao = TWDatabase.instance.sourceDao()

        @JvmStatic fun findByTimelineIdAsync(timelineId: Long) = dao.findByTimelineIdAsync(timelineId)
        @JvmStatic fun find(id: Long) = dao.find(id)
        @JvmStatic fun findEnabled() = dao.findEnabled()
        @JvmStatic fun findByAccountId(accountId: Long) = dao.findByAccountId(accountId)
        @JvmStatic fun save(sources: Collection<Source>) = dao.save(sources)
        @JvmStatic fun delete(sources: Collection<Source>) = dao.delete(sources)
        @JvmStatic fun update(account: Account) = dao.update(account)
    }

    @Ignore
    constructor(account: Account, type: SourceType)
            : this(0, account.id, type.ordinal, type.toString(), "", null, 0, 0, 0, 0, 0)

    fun save() = dao.save(this)
    fun delete() = dao.delete(this)
    fun updateFetchAt() = dao.updateFetchAt(this)

    fun account(): Account = Account.find(accountId)
    fun tweetCount() = Tweet.countBySourceId(id)
    fun maxTweetId() = Tweet.maxIdBySourceId(id)
    fun minTweetId() = Tweet.minIdBySourceId(id)
    fun prevTweetId(targetId: Long) = Tweet.prevIdBySourceId(id, targetId)
}
