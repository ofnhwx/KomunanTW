package net.komunan.komunantw.repository.entity

import androidx.room.*
import net.komunan.komunantw.Preference
import net.komunan.komunantw.repository.database.TWDatabase

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

        @JvmStatic fun findAllAsync() = dao.findAllAsync()
        @JvmStatic fun findByTimelineAsync(timeline: Timeline) = dao.findByTimelineAsync(timeline)
        @JvmStatic fun find(id: Long) = dao.find(id)
        @JvmStatic fun findEnabled() = dao.findEnabled()
        @JvmStatic fun findByAccount(account: Account) = dao.findByAccount(account)
        @JvmStatic fun findByTimeline(timeline: Timeline) = dao.findByTimeline(timeline)
        @JvmStatic fun countByTimeline(timeline: Timeline) = dao.countByTimeline(timeline)
        @JvmStatic fun save(sources: List<Source>) = dao.save(sources)
        @JvmStatic fun delete(sources: List<Source>) = dao.delete(sources)
        @JvmStatic fun updateFor(account: Account) = dao.updateFor(account)
    }

    @Ignore
    constructor(account: Account, type: SourceType): this(
            id = 0,
            accountId = account.id,
            order = type.ordinal,
            type = type.toString(),
            label = "",
            query = null,
            listOwner = 0,
            listId = 0,
            fetchAt = 0,
            createAt = 0,
            updateAt = 0
    )

    fun save() = dao.save(this)
    fun delete() = dao.delete(this)
    fun updateFetchAt() = dao.updateFetchAt(this)

    fun account() = Account.find(accountId)
    fun tweetCount() = Tweet.countBySource(this)
    fun maxTweetId() = Tweet.maxIdBySource(this)
    fun minTweetId() = Tweet.minIdBySource(this)
    fun prevTweetId(targetId: Long) = Tweet.prevIdBySource(this, targetId)

    fun requireAutoFetch() = System.currentTimeMillis() > (fetchAt + Preference.fetchIntervalMillis * Preference.fetchIntervalThreshold)
}
