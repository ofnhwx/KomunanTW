package net.komunan.komunantw.core.repository.entity

import com.github.ajalt.timberkt.i
import io.objectbox.Box
import io.objectbox.annotation.*
import io.objectbox.converter.PropertyConverter
import io.objectbox.kotlin.boxFor
import io.objectbox.query.QueryBuilder
import io.objectbox.relation.ToMany
import io.objectbox.relation.ToOne
import net.komunan.komunantw.R
import net.komunan.komunantw.common.Preference
import net.komunan.komunantw.common.commaSeparated
import net.komunan.komunantw.common.string
import net.komunan.komunantw.core.repository.ObjectBox
import net.komunan.komunantw.core.repository.entity.cache.Tweet
import net.komunan.komunantw.core.repository.entity.cache.Tweet_
import net.komunan.komunantw.ui.common.base.Diffable
import twitter4j.UserList as TwitterUserList

@Entity
class Source() : Diffable {
    @Id
    var id: Long = 0L
    var accountName: String = "" // 整列用
    var ordinal: Int = 0
    @Convert(converter = TypeConverter::class, dbType = String::class)
    var type: Type = Type.HOME
    var label: String = ""
    var query: String? = null
    var listId: Long = 0L
    var listOwner: Long = 0L
    var fetchedAt: Long = 0L
    var savedAt: Long = 0L

    @Transient
    var isActive: Boolean = false

    lateinit var account: ToOne<Account>
    @Backlink
    lateinit var timelines: ToMany<Timeline>

    companion object {
        @JvmStatic
        val box: Box<Source>
            get() = ObjectBox.get().boxFor(Source::class)

        @JvmStatic
        fun query(): QueryBuilder<Source> = box.query()
    }

    constructor(account: Account, type: Type) : this() {
        this.account.target = account
        this.accountName = account.name
        this.type = type
        this.ordinal = type.ordinal
    }

    constructor(account: Account, twitterUserList: TwitterUserList) : this(account, Type.LIST) {
        this.label = twitterUserList.name
        this.listOwner = account.id
        this.listId = twitterUserList.id
    }

    val displayName: String
        get() {
            return when (type) {
                Type.HOME -> string[R.string.source_name_home]()
                Type.MENTION -> string[R.string.source_name_mention]()
                Type.USER -> string[R.string.source_name_user]()
                Type.LIKE -> string[R.string.source_name_like]()
                Type.LIST -> string[R.string.source_name_list](label)
                Type.SEARCH -> string[R.string.source_name_search](label)
            }
        }

    val requireAutoFetch: Boolean
        get() {
            val elapsed = (System.currentTimeMillis() - fetchedAt)
            val required = (Preference.fetchIntervalMillis * Preference.fetchIntervalThreshold).toLong()
            return elapsed >= required
        }

    fun save(): Source {
        box.put(apply {
            savedAt = System.currentTimeMillis()
        })
        return this
    }

    fun addMissing(tweetId: Long) {
        Tweet.box.get(tweetId).missings.apply {
            add(this@Source)
            applyChangesToDb()
        }
        i { "Missing: add={ source=$id, tweet=${tweetId.commaSeparated()} }" }
    }

    fun delMissing(tweetId: Long) {
        Tweet.box.get(tweetId).missings.apply {
            remove(this@Source)
            applyChangesToDb()
        }
        i { "Missing: del={ source=$id, tweet=${tweetId.commaSeparated()} }" }
    }

    fun newestTweetId(): Long {
        val query = Tweet.query().apply {
            link(Tweet_.sources).equal(Source_.id, this@Source.id)
        }.build()
        return if (query.count() == 0L) {
            Tweet.INVALID_ID
        } else {
            query.property(Tweet_.id).max()
        }
    }

    fun oldestTweetId(): Long {
        val query = Tweet.query().apply {
            link(Tweet_.sources).equal(Source_.id, this@Source.id)
        }.build()
        return if (query.count() == 0L) {
            Tweet.INVALID_ID
        } else {
            query.property(Tweet_.id).min()
        }
    }

    fun previousTweetId(tweetId: Long): Long {
        val query = Tweet.query().apply {
            link(Tweet_.sources).equal(Source_.id, this@Source.id)
            less(Tweet_.id, tweetId)
        }.build()
        return if (query.count() == 0L) {
            Tweet.INVALID_ID
        } else {
            query.property(Tweet_.id).max()
        }
    }

    fun tweetCount(): Long {
        return Tweet.query().apply {
            link(Tweet_.sources).equal(Source_.id, this@Source.id)
        }.build().count()
    }

    override fun equals(other: Any?): Boolean {
        return other is Source
                && this.id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun isTheSame(other: Diffable): Boolean {
        return this == other
    }

    override fun isContentsTheSame(other: Diffable): Boolean {
        return false
    }

    enum class Type(val standard: Boolean, val editable: Boolean) {
        HOME(true, false),
        MENTION(true, false),
        USER(true, false),
        LIKE(true, false),
        LIST(false, false),
        SEARCH(false, true),
    }

    class TypeConverter : PropertyConverter<Type, String> {
        override fun convertToEntityProperty(value: String): Type {
            return Type.valueOf(value)
        }

        override fun convertToDatabaseValue(value: Type): String {
            return value.toString()
        }
    }
}
