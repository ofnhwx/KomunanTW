package net.komunan.komunantw.repository.entity

import androidx.room.*
import net.komunan.komunantw.Preference
import net.komunan.komunantw.repository.database.TWDatabase
import net.komunan.komunantw.toBoolean
import net.komunan.komunantw.toInt

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
        ],
        indices = [
            Index("account_id")
        ]
)
open class Source() {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")         var id       : Long = 0
    @ColumnInfo(name = "account_id") var accountId: Long = 0
    @ColumnInfo(name = "order")      var order    : Int = 0
    @ColumnInfo(name = "type")       var type     : String = ""
    @ColumnInfo(name = "label")      var label    : String = ""
    @ColumnInfo(name = "query")      var query    : String? = null
    @ColumnInfo(name = "list_id")    var listId   : Long = 0
    @ColumnInfo(name = "list_owner") var listOwner: Long = 0
    @ColumnInfo(name = "fetch_at")   var fetchAt  : Long = 0
    @ColumnInfo(name = "create_at")  var createAt : Long = 0
    @ColumnInfo(name = "update_at")  var updateAt : Long = 0

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
        @JvmStatic fun findForTimelineEditAsync(timeline: Timeline) = dao.findForTimelineEditAsync(timeline)
        @JvmStatic fun countByTimeline(timeline: Timeline) = dao.countByTimeline(timeline)
        @JvmStatic fun save(sources: List<Source>) = dao.save(sources)
        @JvmStatic fun delete(sources: List<Source>) = dao.delete(sources)
        @JvmStatic fun updateFor(account: Account) = dao.updateFor(account)
    }

    @Ignore
    constructor(account: Account, type: SourceType): this() {
        this.accountId = account.id
        this.order = type.ordinal
        this.type = type.toString()
    }

    fun save() = dao.save(this)
    fun delete() = dao.delete(this)
    fun updateFetchAt() = dao.updateFetchAt(this)

    fun account() = Account.find(accountId)
    fun tweetCount() = Tweet.countBySource(this)
    fun maxTweetId() = Tweet.maxIdBySource(this)
    fun minTweetId() = Tweet.minIdBySource(this)
    fun prevTweetId(targetId: Long) = Tweet.prevIdBySource(this, targetId)

    fun requireAutoFetch() = System.currentTimeMillis() > (fetchAt + Preference.fetchIntervalMillis * Preference.fetchIntervalThreshold)

    override fun toString(): String {
        return "${Source::class.simpleName}{ " +
                "id=$id, " +
                "accountId=$accountId, " +
                "order=$order, " +
                "type=$type, " +
                "label=$label, " +
                "query=$query, " +
                "listId=$listId, " +
                "listOwner=$listId, " +
                "fetchAt=$fetchAt, " +
                "createAt=$createAt, " +
                "updateAt=$updateAt }"
    }
}

@Suppress("PropertyName")
class SourceForSelect(): Source() {
    @ColumnInfo(name = "is_active") var _isActive: Int = 0

    var isActive: Boolean
        get() = _isActive.toBoolean()
        set(value) {
            _isActive = value.toInt()
        }

    override fun toString(): String {
        return "${SourceForSelect::class.simpleName}{ base=${super.toString()}, isActive=$isActive }"
    }
}
