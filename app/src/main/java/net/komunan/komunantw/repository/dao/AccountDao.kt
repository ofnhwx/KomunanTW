package net.komunan.komunantw.repository.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.github.ajalt.timberkt.d
import net.komunan.komunantw.repository.entity.Account

@Dao
abstract class AccountDao {
    @Query("SELECT * FROM account ORDER BY name ASC")
    abstract fun findAllAsync(): LiveData<List<Account>>

    @Query("SELECT * FROM account WHERE id = :id")
    abstract fun find(id: Long): Account?

    @Query("SELECT COUNT(*) FROM account")
    abstract fun count(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract fun _insert(account: Account)

    @Update
    abstract fun _update(account: Account)

    @Delete
    abstract fun delete(account: Account)

    fun save(account: Account): Account {
        val current = find(account.id)
        if (current == null) {
            _insert(account.apply {
                createAt = System.currentTimeMillis()
            })
        } else {
            _update(account.apply {
                createAt = current.createAt
                updateAt = System.currentTimeMillis()
            })
        }
        d { "save: $account" }
        return account
    }
}
