package net.komunan.komunantw.repository.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import net.komunan.komunantw.repository.entity.ConsumerKeySecret

@Dao
interface ConsumerKeySecretDao {
    @Query("SELECT COUNT(*) FROM ConsumerKeySecret")
    fun count(): Int

    @Query("SELECT * FROM ConsumerKeySecret ORDER BY is_default DESC, name ASC")
    fun findAll(): LiveData<List<ConsumerKeySecret>>

    @Query("SELECT * FROM ConsumerKeySecret WHERE id = :id")
    fun find(id: Long): LiveData<ConsumerKeySecret>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(consumerKeySecret: ConsumerKeySecret)

    @Delete
    fun delete(consumerKeySecret: ConsumerKeySecret)
}
