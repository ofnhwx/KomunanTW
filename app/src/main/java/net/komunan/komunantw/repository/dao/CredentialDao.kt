package net.komunan.komunantw.repository.dao

import android.arch.persistence.room.*
import com.github.ajalt.timberkt.d
import net.komunan.komunantw.repository.entity.Credential

@Dao
abstract class CredentialDao {
    @Query("SELECT * FROM credential WHERE account_id = :accountId")
    abstract fun findByAccountId(accountId: Long): List<Credential>

    @Query("SELECT * FROM credential WHERE account_id = :accountId AND consumer_key = :consumerKey AND consumer_secret = :consumerSecret")
    abstract fun findSame(accountId: Long, consumerKey: String, consumerSecret: String): Credential?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract fun _insert(credential: Credential)

    @Update
    abstract fun _update(credential: Credential)

    @Delete
    abstract fun delete(credential: Credential)

    fun save(credential: Credential): Credential {
        val current = findSame(credential.accountId, credential.consumerKey, credential.consumerSecret)
        if (current == null) {
            _insert(credential.apply {
                createAt = System.currentTimeMillis()
            })
        } else {
            _update(credential.apply {
                createAt = current.createAt
                updateAt = System.currentTimeMillis()
            })
        }
        d { "save: $credential" }
        return credential
    }
}
