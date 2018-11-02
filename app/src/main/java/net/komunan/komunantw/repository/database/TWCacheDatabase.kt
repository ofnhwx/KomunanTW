package net.komunan.komunantw.repository.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import net.komunan.komunantw.repository.dao.TweetDao
import net.komunan.komunantw.repository.dao.TweetSourceDao
import net.komunan.komunantw.repository.dao.UserDao
import net.komunan.komunantw.repository.entity.Tweet
import net.komunan.komunantw.repository.entity.TweetSource
import net.komunan.komunantw.repository.entity.User

@Database(
        entities = [
            Tweet::class,
            TweetSource::class,
            User::class
        ],
        version = 1,
        exportSchema = false
)
abstract class TWCacheDatabase: RoomDatabase() {
    companion object {
        @JvmStatic
        val instance by lazy { TWBaseDatabase.getInstance(TWCacheDatabase::class.java) }
    }

    abstract fun tweetDao(): TweetDao
    abstract fun tweetSourceDao(): TweetSourceDao
    abstract fun userDao(): UserDao
}
