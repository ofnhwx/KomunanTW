package net.komunan.komunantw.repository.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import net.komunan.komunantw.repository.entity.Account
import net.komunan.komunantw.repository.entity.AccountWithCredential

@Dao
interface AccountDao {
    @Query("SELECT COUNT(*) FROM Account")
    fun count(): Int

    @Transaction
    @Query("SELECT * FROM Account ORDER BY name ASC")
    fun findAll(): LiveData<List<AccountWithCredential>>

    @Transaction
    @Query("SELECT * FROM Account WHERE id = :id")
    fun find(id: Long): LiveData<AccountWithCredential>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(account: Account)

    @Delete
    fun delete(account: Account)
}
