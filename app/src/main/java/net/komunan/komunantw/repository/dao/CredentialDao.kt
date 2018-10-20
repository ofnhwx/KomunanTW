package net.komunan.komunantw.repository.dao

import android.arch.persistence.room.*
import net.komunan.komunantw.repository.entity.Credential

@Dao
interface CredentialDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(credential: Credential)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(credentials: List<Credential>)

    @Delete
    fun delete(credential: Credential)

    @Delete
    fun delete(credentials: List<Credential>)
}
