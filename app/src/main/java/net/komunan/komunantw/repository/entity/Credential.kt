package net.komunan.komunantw.repository.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Ignore
import com.github.ajalt.timberkt.d
import net.komunan.komunantw.repository.database.TWDatabase
import net.komunan.komunantw.service.TwitterService
import twitter4j.auth.AccessToken

@Entity(
        tableName = "credential",
        primaryKeys = ["account_id", "consumer_key", "consumer_secret"],
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
data class Credential(
        @ColumnInfo(name = "account_id")
        var accountId: Long,
        @ColumnInfo(name = "consumer_key")
        var consumerKey: String,
        @ColumnInfo(name = "consumer_secret")
        var consumerSecret: String,
        @ColumnInfo(name = "token")
        var token: String,
        @ColumnInfo(name = "token_secret")
        var tokenSecret: String,
        @ColumnInfo(name = "create_at")
        var createAt: Long,
        @ColumnInfo(name = "update_at")
        var updateAt: Long
) {
    companion object {
        private fun dao() = TWDatabase.instance.credentialDao()

        @JvmStatic
        fun findByAccountId(accountId: Long) = dao().findByAccountId(accountId)

        @JvmStatic
        fun save(credentials: Collection<Credential>) = dao().save(credentials)

        @JvmStatic
        fun delete(credentials: Collection<Credential>) = dao().delete(credentials)
    }

    @Ignore
    constructor(account: Account, consumerKeySecret: ConsumerKeySecret, accessToken: AccessToken)
        : this(account.id, consumerKeySecret.consumerKey, consumerKeySecret.consumerSecret, accessToken.token, accessToken.tokenSecret, 0, 0)

    fun save(): Credential {
        dao().save(this)
        d { "save: $this" }
        return this
    }

    fun delete() = dao().delete(this)

}
