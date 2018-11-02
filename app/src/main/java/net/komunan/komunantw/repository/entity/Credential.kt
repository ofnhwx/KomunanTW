package net.komunan.komunantw.repository.entity

import androidx.room.*
import net.komunan.komunantw.repository.database.TWDatabase
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
        private val dao = TWDatabase.instance.credentialDao()

        @JvmStatic fun findByAccountId(accountId: Long) = dao.findByAccountId(accountId)
    }

    @Ignore
    constructor(account: Account, consumerKeySecret: ConsumerKeySecret, accessToken: AccessToken)
        : this(account.id, consumerKeySecret.consumerKey, consumerKeySecret.consumerSecret, accessToken.token, accessToken.tokenSecret, 0, 0)

    fun save(): Credential = dao.save(this)
    fun delete() = dao.delete(this)

}
