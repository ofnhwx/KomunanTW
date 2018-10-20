package net.komunan.komunantw.repository.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import net.komunan.komunantw.repository.entity.Source

@Dao
interface SourceDao {
    @Query("SELECT * FROM Source WHERE id in (:ids)")
    fun findByIds(ids: List<Long>): LiveData<List<Source>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(source: Source)

    @Delete
    fun delete(source: Source)
}
