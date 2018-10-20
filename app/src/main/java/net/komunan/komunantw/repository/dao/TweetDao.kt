package net.komunan.komunantw.repository.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import net.komunan.komunantw.repository.entity.Tweet

@Dao
interface TweetDao {
    @Query("SELECT * FROM Tweet WHERE id = :id")
    fun find(id: Long): LiveData<List<Tweet>>

    @Query("SELECT * FROM Tweet WHERE source_id in (:sourceIds)")
    fun timelineColumnTweets(sourceIds: List<Long>): LiveData<List<Tweet>>
}
