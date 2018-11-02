package net.komunan.komunantw.repository.dao;

import androidx.room.*
import net.komunan.komunantw.repository.entity.User

@Suppress("FunctionName")
@Dao
abstract class UserDao {
    fun find(id: Long) = __find(id)
    fun save(users: List<User>) = __save(users)

    /* ==================== SQL Definitions. ==================== */

    @Query("SELECT * FROM user WHERE id = :id")
    protected abstract fun __find(id: Long): User?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun __save(users: List<User>)
}
