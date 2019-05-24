package net.komunan.komunantw.core.repository.entity

import io.objectbox.Box
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.kotlin.boxFor
import io.objectbox.query.QueryBuilder
import io.objectbox.relation.ToOne
import net.komunan.komunantw.core.repository.ObjectBox
import twitter4j.auth.AccessToken

@Entity
class Credential() {
    @Id
    var id: Long = 0L
    var consumerKey: String = ""
    var consumerSecret: String = ""
    var token: String = ""
    var tokenSecret: String = ""
    var savedAt: Long = 0L

    lateinit var account: ToOne<Account>

    companion object {
        @JvmStatic
        val box: Box<Credential>
            get() = ObjectBox.get().boxFor(Credential::class)

        @JvmStatic
        fun query(): QueryBuilder<Credential> = box.query()
    }

    constructor(account: Account, consumer: Consumer, accessToken: AccessToken) : this() {
        this.account.target = account
        this.consumerKey = consumer.key
        this.consumerSecret = consumer.secret
        this.token = accessToken.token
        this.tokenSecret = accessToken.tokenSecret
    }

    fun save(): Credential {
        box.put(apply {
            savedAt = System.currentTimeMillis()
        })
        return this
    }

    override fun equals(other: Any?): Boolean {
        return other is Credential
                && this.id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
