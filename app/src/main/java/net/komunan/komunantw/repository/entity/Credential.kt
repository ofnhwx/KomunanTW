package net.komunan.komunantw.repository.entity

import android.arch.persistence.room.*
import net.komunan.komunantw.repository.database.TWDatabase
import twitter4j.auth.AccessToken

@Entity(
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
class Credential {
    @ColumnInfo(name = "account_id")
    var accountId: Long = 0
    @ColumnInfo(name = "consumer_key")
    var consumerKey: String = ""
    @ColumnInfo(name = "consumer_secret")
    var consumerSecret: String = ""
    var token: String = ""
    @ColumnInfo(name = "token_secret")
    var tokenSecret: String = ""
    @Ignore
    val accessToken: AccessToken = AccessToken(token, tokenSecret)

    fun save() = TWDatabase.instance.credentialDao().save(this)
    fun delete() = TWDatabase.instance.credentialDao().delete(this)
}
