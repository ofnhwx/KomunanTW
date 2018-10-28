package net.komunan.komunantw.repository.dao

import android.arch.persistence.room.*
import com.github.ajalt.timberkt.d
import net.komunan.komunantw.repository.entity.Credential

@Dao
abstract class CredentialDao {
    @Query("SELECT * FROM credential WHERE account_id = :accountId")
    abstract fun findByAccountId(accountId: Long): List<Credential>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract fun _insert(credential: Credential)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract fun _insert(credentials: Collection<Credential>)

    @Update
    abstract fun _update(credential: Credential)

    @Update
    abstract fun _update(credentials: Collection<Credential>)

    @Delete
    abstract fun delete(credential: Credential)

    @Delete
    abstract fun delete(credentials: Collection<Credential>)

    fun save(credential: Credential): Credential {
        if (credential.createAt == 0L) {
            _insert(credential.apply {
                createAt = System.currentTimeMillis()
            })
        } else {
            _update(credential.apply {
                updateAt = System.currentTimeMillis()
            })
        }
        d { "save: $credential" }
        return credential
    }

    fun save(credentials: Collection<Credential>) {
        _update(credentials.filter { it.createAt != 0L }.map { it.apply { updateAt = System.currentTimeMillis() } })
        _insert(credentials.filter { it.createAt == 0L }.map { it.apply { createAt = System.currentTimeMillis() } })
    }
}
