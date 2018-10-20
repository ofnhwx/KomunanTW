package net.komunan.komunantw.repository.dao

import android.arch.persistence.room.*
import net.komunan.komunantw.repository.entity.ColumnSource

@Dao
interface ColumnSourceDao {
    @Query("SELECT COUNT(*) FROM ColumnSource")
    fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(columnSource: ColumnSource)

    @Delete
    fun delete(columnSource: ColumnSource)
}
