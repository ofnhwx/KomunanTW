package net.komunan.komunantw.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.github.ajalt.timberkt.d
import net.komunan.komunantw.repository.entity.Account

@Suppress("FunctionName")
@Dao
abstract class AccountDao {
    fun findAllAsync() = __findAllAsync()
    fun find(id: Long) = __find(id)
    fun count() = __count()
    fun save(account: Account) = __save__(account)
    fun delete(account: Account) = __delete(account)

    /* ==================== SQL Definitions. ==================== */

    @Query("SELECT * FROM account ORDER BY name ASC")
    protected abstract fun __findAllAsync(): LiveData<List<Account>>
    @Query("SELECT * FROM account WHERE id = :id")
    protected abstract fun __find(id: Long): Account?
    @Query("SELECT COUNT(*) FROM account")
    protected abstract fun __count(): Int
    @Insert(onConflict = OnConflictStrategy.ABORT)
    protected abstract fun __insert(account: Account)
    @Update
    protected abstract fun __update(account: Account)
    @Delete
    protected abstract fun __delete(account: Account)

    /* ==================== Private Functions. ==================== */

    private fun __save__(account: Account): Account {
        val current = find(account.id)
        if (current == null) {
            __insert(account.apply {
                createAt = System.currentTimeMillis()
            })
        } else {
            __update(account.apply {
                createAt = current.createAt
                updateAt = System.currentTimeMillis()
            })
        }
        d { "save: $account" }
        return account
    }
}
