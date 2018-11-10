package net.komunan.komunantw.repository.entity

import androidx.room.*
import net.komunan.komunantw.repository.database.TWDatabase
import org.apache.commons.lang3.builder.ToStringBuilder
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
class Credential() {
    @ColumnInfo(name = "account_id")      var accountId     : Long = 0L
    @ColumnInfo(name = "consumer_key")    var consumerKey   : String = ""
    @ColumnInfo(name = "consumer_secret") var consumerSecret: String = ""
    @ColumnInfo(name = "token")           var token         : String = ""
    @ColumnInfo(name = "token_secret")    var tokenSecret   : String = ""
    @ColumnInfo(name = "create_at")       var createAt      : Long = 0L
    @ColumnInfo(name = "update_at")       var updateAt      : Long = 0L

    companion object {
        @JvmStatic
        val dao = TWDatabase.instance.credentialDao()
    }

    @Ignore
    constructor(account: Account, consumer: Consumer, accessToken: AccessToken): this() {
        this.accountId = account.id
        this.consumerKey = consumer.key
        this.consumerSecret = consumer.secret
        this.token = accessToken.token
        this.tokenSecret = accessToken.tokenSecret
    }

    fun save(): Credential = dao.save(this)
    fun delete() = dao.delete(this)

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }
}
