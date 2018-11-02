package net.komunan.komunantw.repository.dao;

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import net.komunan.komunantw.repository.entity.User

@Dao
interface UserDao {
    @Query("SELECT * FROM user WHERE id = :id")
    fun find(id: Long): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(users: Collection<User>)
}
