package net.komunan.komunantw.repository.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query;
import net.komunan.komunantw.repository.entity.User

@Dao
interface UserDao {
    @Query("SELECT * FROM user WHERE id = :id")
    fun find(id: Long): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(users: Collection<User>)
}
