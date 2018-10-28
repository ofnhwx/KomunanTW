package net.komunan.komunantw.repository.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.github.ajalt.timberkt.d
import net.komunan.komunantw.repository.entity.ConsumerKeySecret

@Dao
abstract class ConsumerKeySecretDao {
    @Query("SELECT * FROM consumer_key_secret ORDER BY is_default DESC, name ASC")
    abstract fun findAllAsync(): LiveData<List<ConsumerKeySecret>>

    @Query("SELECT * FROM consumer_key_secret ORDER BY is_default DESC, name ASC")
    abstract fun findAll(): List<ConsumerKeySecret>

    @Query("SELECT COUNT(*) FROM consumer_key_secret")
    abstract fun count(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract fun _insert(consumerKeySecret: ConsumerKeySecret): Long

    @Update
    abstract fun _update(consumerKeySecret: ConsumerKeySecret)

    @Delete
    abstract fun _delete(consumerKeySecret: ConsumerKeySecret)

    fun save(consumerKeySecret: ConsumerKeySecret): ConsumerKeySecret {
        if (consumerKeySecret.id == 0L) {
            consumerKeySecret.id = _insert(consumerKeySecret.apply {
                createAt = System.currentTimeMillis()
            })
        } else {
            _update(consumerKeySecret.apply {
                updateAt = System.currentTimeMillis()
            })
        }
        d { "save: $consumerKeySecret" }
        return consumerKeySecret
    }

    fun delete(consumerKeySecret: ConsumerKeySecret) {
        if (consumerKeySecret.default) {
            return
        }
        _delete(consumerKeySecret)
    }
}
