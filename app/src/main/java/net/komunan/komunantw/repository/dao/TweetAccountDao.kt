package net.komunan.komunantw.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import net.komunan.komunantw.repository.entity.TweetAccount

@Dao
abstract class TweetAccountDao {

    /* ==================== SQL Definitions. ==================== */

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun save(tweetAccount: TweetAccount)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun save(tweetAccounts: List<TweetAccount>)
}
