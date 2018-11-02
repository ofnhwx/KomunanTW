package net.komunan.komunantw.repository.dao

import androidx.room.*
import com.github.ajalt.timberkt.d
import net.komunan.komunantw.repository.entity.Account
import net.komunan.komunantw.repository.entity.Credential

@Suppress("FunctionName")
@Dao
abstract class CredentialDao {
    fun findByAccount(account: Account): List<Credential> = __findByAccountId(account.id)
    fun save(credential: Credential): Credential = __save__(credential)
    fun delete(credential: Credential) = __delete(credential)

    /* ==================== SQL Definitions. ==================== */

    @Query("SELECT * FROM credential WHERE account_id = :accountId")
    protected abstract fun __findByAccountId(accountId: Long): List<Credential>
    @Query("SELECT * FROM credential WHERE account_id = :accountId AND consumer_key = :consumerKey AND consumer_secret = :consumerSecret")
    protected abstract fun __findSame(accountId: Long, consumerKey: String, consumerSecret: String): Credential?
    @Insert(onConflict = OnConflictStrategy.ABORT)
    protected abstract fun __insert(credential: Credential)
    @Update
    protected abstract fun __update(credential: Credential)
    @Delete
    protected abstract fun __delete(credential: Credential)

    /* ==================== Private Functions. ==================== */

    private fun findSame(credential: Credential): Credential? = __findSame(credential.accountId, credential.consumerKey, credential.consumerSecret)

    private fun __save__(credential: Credential): Credential {
        val current = findSame(credential)
        if (current == null) {
            __insert(credential.apply {
                createAt = System.currentTimeMillis()
            })
        } else {
            __update(credential.apply {
                createAt = current.createAt
                updateAt = System.currentTimeMillis()
            })
        }
        d { "save: $credential" }
        return credential
    }
}
