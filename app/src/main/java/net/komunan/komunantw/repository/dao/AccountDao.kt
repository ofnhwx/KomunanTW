package net.komunan.komunantw.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.github.ajalt.timberkt.d
import net.komunan.komunantw.repository.entity.Account

@Suppress("unused")
@Dao
abstract class AccountDao {

    /* ==================== Functions. ==================== */

    fun save(account: Account): Account {
        val current = find(account.id)
        if (current == null) {
            pInsert(account.apply {
                createAt = System.currentTimeMillis()
            })
        } else {
            pUpdate(account.apply {
                createAt = current.createAt
                updateAt = System.currentTimeMillis()
            })
        }
        d { "save: $account" }
        return account
    }

    /* ==================== SQL Definitions. ==================== */

    companion object {
        private const val QUERY_COUNT    = "SELECT COUNT(*) FROM account"
        private const val QUERY_FIND     = "SELECT * FROM account WHERE id = :id"
        private const val QUERY_FIND_IDS = "SELECT * FROM account WHERE id IN (:ids) ORDER BY name ASC"
        private const val QUERY_FIND_ALL = "SELECT * FROM account ORDER BY name ASC"
    }

    @Query(QUERY_COUNT)
    abstract fun count(): Int

    @Query(QUERY_FIND)
    abstract fun find(id: Long): Account?

    @Query(QUERY_FIND_IDS)
    abstract fun find(ids: List<Long>): List<Account>

    @Query(QUERY_FIND_ALL)
    abstract fun findAll(): List<Account>

    @Query(QUERY_FIND_ALL)
    abstract fun findAllAsync(): LiveData<List<Account>>

    @Delete
    abstract fun delete(account: Account)

    /* ==================== Protected SQL Definitions. ==================== */

    @Insert(onConflict = OnConflictStrategy.ABORT)
    protected abstract fun pInsert(account: Account)

    @Update
    protected abstract fun pUpdate(account: Account)
}
