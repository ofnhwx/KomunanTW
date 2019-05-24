package net.komunan.komunantw.core.repository.entity

import io.objectbox.Box
import io.objectbox.annotation.Backlink
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.kotlin.boxFor
import io.objectbox.query.QueryBuilder
import io.objectbox.relation.ToMany
import net.komunan.komunantw.core.repository.ObjectBox
import net.komunan.komunantw.ui.common.base.Diffable
import twitter4j.User as TwitterUser

@Entity
class Account() : Diffable {
    @Id(assignable = true)
    var id: Long = 0L
    var name: String = ""
    var screenName: String = ""
    var imageUrl: String = ""
    var savedAt: Long = 0L

    @Backlink
    lateinit var credentials: ToMany<Credential>
    @Backlink
    lateinit var sources: ToMany<Source>

    companion object {
        @JvmStatic
        val box: Box<Account>
            get() = ObjectBox.get().boxFor(Account::class)

        @JvmStatic
        fun query(): QueryBuilder<Account> = box.query()
    }

    constructor(twitterUser: TwitterUser) : this() {
        this.id = twitterUser.id
        this.name = twitterUser.name
        this.screenName = twitterUser.screenName
        this.imageUrl = twitterUser.biggerProfileImageURLHttps
    }

    fun save(): Account {
        box.put(apply {
            savedAt = System.currentTimeMillis()
        })
        return this
    }

    override fun equals(other: Any?): Boolean {
        return other is Account
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
}
