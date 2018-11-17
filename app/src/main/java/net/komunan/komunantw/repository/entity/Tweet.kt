package net.komunan.komunantw.repository.entity

import androidx.room.*
import net.komunan.komunantw.common.extension.TransactionTarget
import net.komunan.komunantw.common.extension.gson
import net.komunan.komunantw.common.extension.transaction
import net.komunan.komunantw.repository.database.TWCacheDatabase
import net.komunan.komunantw.repository.entity.ext.TweetExtension
import org.apache.commons.lang3.builder.ToStringBuilder
import twitter4j.MediaEntity
import twitter4j.Status
import twitter4j.URLEntity

@Suppress("PropertyName")
@Entity(tableName = "tweet", primaryKeys = ["id"])
open class Tweet() {
    @ColumnInfo(name = "id")            var id          : Long = 0L
    @ColumnInfo(name = "user_id")       var userId      : Long = 0L
    @ColumnInfo(name = "text")          var text        : String = ""
    @ColumnInfo(name = "retweet_count") var retweetCount: Int = 0
    @ColumnInfo(name = "like_count")    var likeCount   : Int = 0
    @ColumnInfo(name = "timestamp")     var timestamp   : Long = 0L
    @ColumnInfo(name = "via")           var via         : String = ""
    @ColumnInfo(name = "reply_id")      var replyId     : Long = 0L
    @ColumnInfo(name = "reply_user_id") var replyUserId : Long = 0L
    @ColumnInfo(name = "rt_id")         var rtId        : Long = 0L
    @ColumnInfo(name = "rt_user_id")    var rtUserId    : Long = 0L
    @ColumnInfo(name = "qt_id")         var qtId        : Long = 0L
    @ColumnInfo(name = "qt_user_id")    var qtUserId    : Long = 0L
    @ColumnInfo(name = "ext")           var ext         : TweetExtension = TweetExtension()

    companion object {
        @JvmStatic val INVALID_ID: Long = -1L
        @JvmStatic val dao = TWCacheDatabase.instance.tweetDao()
        @JvmStatic val accountDao = TWCacheDatabase.instance.tweetAccountDao()
        @JvmStatic val sourceDao = TWCacheDatabase.instance.tweetSourceDao()

        @JvmStatic
        fun createCache(source: Source, statuses: List<Status>) = transaction(TransactionTarget.WITH_CACHE) {
            val account = Account.dao.find(source.accountId)!!
            statuses.forEach { dao.saveWithoutLog(Tweet(it)) }
            accountDao.save(statuses.map { TweetAccount(account.id, it) })
            sourceDao.save(statuses.map { TweetSource(source.id, it.id) })
        }
    }

    @Ignore
    constructor(status: Status): this() {
        (status.retweetedStatus ?: status).also {
            this.id = status.id
            this.userId = status.user.id
            this.text = it.text
            this.retweetCount = it.retweetCount
            this.likeCount = it.favoriteCount
            this.via = it.source
            this.timestamp = it.createdAt.time
            this.ext = TweetExtension(it)
            // 返信に関する情報
            this.replyId = it.inReplyToStatusId
            this.replyUserId = it.inReplyToUserId
            // リツイートに関する情報
            if (status.isRetweet) {
                this.rtId = it.id
                this.rtUserId = it.user.id
            }
            // 引用ツイートに関する情報
            it.quotedStatus?.let { qt ->
                this.qtId = qt.id
                this.qtUserId = qt.user.id
            }
        }
    }

    val mainUserId: Long
        get() = if (isRetweet) rtUserId else userId

    val displayText: String
        get() {
            var result = text
            ext.urls.forEach { result = result.replace(it.shorten, it.display) }
            ext.medias.forEach { result = result.replace(it.shorten, "") }
            return result
        }

    val isReply: Boolean
        get() = replyId != -1L

    val isRetweet: Boolean
        get() = rtId != 0L

    val hasQuote: Boolean
        get() = qtId != 0L

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }

    fun save() = dao.save(this)
}
