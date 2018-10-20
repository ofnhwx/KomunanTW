package net.komunan.komunantw.repository.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import net.komunan.komunantw.repository.entity.Column

@Dao
interface ColumnDao {
    @Query("SELECT COUNT(*) FROM `Column`")
    fun count(): Int

    @Query("SELECT * FROM `Column` ORDER BY position ASC")
    fun findAll(): LiveData<List<Column>>

    @Query("SELECT * FROM `Column` WHERE id = :id")
    fun find(id: Long): LiveData<Column>

    @Query("UPDATE `Column` SET position = position - 1 WHERE position > :deleted")
    fun packPosition(deleted: Int)

    @Query("UPDATE `Column` SET position = position - 1 WHERE position BETWEEN :begin AND :end")
    fun shiftLeft(begin: Int, end: Int)

    @Query("UPDATE `Column` SET position = position + 1 WHERE position BETWEEN :begin AND :end")
    fun shiftRight(begin: Int, end: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(column: Column)

    @Delete
    fun delete(column: Column)
}
