package net.komunan.komunantw.repository.dao

import androidx.room.*
import com.github.ajalt.timberkt.d
import net.komunan.komunantw.repository.entity.Credential

@Suppress("unused")
@Dao
abstract class CredentialDao {

    /* ==================== Private Functions. ==================== */

    fun save(credential: Credential): Credential {
        val current = pFindSame(credential.accountId, credential.consumerKey, credential.consumerSecret)
        if (current == null) {
            pInsert(credential.apply {
                createAt = System.currentTimeMillis()
            })
        } else {
            pUpdate(credential.apply {
                createAt = current.createAt
                updateAt = System.currentTimeMillis()
            })
        }
        d { "save: $credential" }
        return credential
    }

    /* ==================== SQL Definitions. ==================== */

    companion object {
        private const val QUERY_FIND_BY_ACCOUNT_ID = "SELECT * FROM credential WHERE account_id = :accountId"
        private const val QUERY_FIND_SAME = "SELECT * FROM credential WHERE account_id = :accountId AND consumer_key = :consumerKey AND consumer_secret = :consumerSecret"
    }

    @Query(QUERY_FIND_BY_ACCOUNT_ID)
    abstract fun findByAccountId(accountId: Long): List<Credential>

    @Delete
    abstract fun delete(credential: Credential)

    /* ==================== Protected SQL Definitions. ==================== */

    @Query(QUERY_FIND_SAME)
    protected abstract fun pFindSame(accountId: Long, consumerKey: String, consumerSecret: String): Credential?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    protected abstract fun pInsert(credential: Credential)

    @Update
    protected abstract fun pUpdate(credential: Credential)
}
