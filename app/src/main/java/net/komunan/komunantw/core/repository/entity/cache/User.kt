package net.komunan.komunantw.core.repository.entity.cache

import io.objectbox.Box
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.kotlin.boxFor
import io.objectbox.query.QueryBuilder
import net.komunan.komunantw.R
import net.komunan.komunantw.common.string
import net.komunan.komunantw.common.uri
import net.komunan.komunantw.core.repository.ObjectBox
import twitter4j.Status as TwitterStatus
import twitter4j.User as TwitterUser

@Entity
class User() {
    @Id(assignable = true)
    var id: Long = 0L
    var name: String = ""
    var screenName: String = ""
    var imageUrl: String = ""
    var cachedAt: Long = 0L

    companion object {
        @JvmStatic
        val box: Box<User>
            get() = ObjectBox.get().boxFor(User::class)

        @JvmStatic
        fun query(): QueryBuilder<User> = box.query()

        @JvmStatic
        val dummy: User
            get() = User().apply {
                name = string[R.string.dummy]()
                screenName = string[R.string.dummy]()
                imageUrl = uri[R.mipmap.ic_launcher].toString()
            }

        @JvmStatic
        fun createCache(statuses: List<TwitterStatus>) {
            val users1 = statuses.map { User(it.user) }
            val users2 = statuses.mapNotNull(TwitterStatus::getRetweetedStatus).map { User(it.user) }
            val users3 = statuses.mapNotNull(TwitterStatus::getQuotedStatus).map { User(it.user) }
            save(users1.plus(users2).plus(users3).distinctBy(User::id))
        }

        @JvmStatic
        fun save(users: List<User>) {
            users.forEach { it.cachedAt = System.currentTimeMillis() }
            box.put(users)
        }
    }

    constructor(twitterUser: TwitterUser) : this() {
        this.id = twitterUser.id
        this.name = twitterUser.name
        this.screenName = twitterUser.screenName
        this.imageUrl = twitterUser.biggerProfileImageURLHttps
    }

    fun save(): User {
        box.put(apply {
            cachedAt = System.currentTimeMillis()
        })
        return this
    }

    override fun equals(other: Any?): Boolean {
        return other is User
                && this.id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
