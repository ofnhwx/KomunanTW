package net.komunan.komunantw.repository.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import net.komunan.komunantw.repository.dao.TweetAccountDao
import net.komunan.komunantw.repository.dao.TweetDao
import net.komunan.komunantw.repository.dao.TweetSourceDao
import net.komunan.komunantw.repository.dao.UserDao
import net.komunan.komunantw.repository.entity.*

@Database(
        entities = [
            Tweet::class,
            TweetAccount::class,
            TweetSource::class,
            User::class
        ],
        version = 1,
        exportSchema = false
)
@TypeConverters(Tweet.Extension.Converter::class)
abstract class TWCacheDatabase: RoomDatabase() {
    @Suppress("ObjectPropertyName")
    companion object {
        private var _instance: TWCacheDatabase? = null

        @JvmStatic
        val instance: TWCacheDatabase
            @Synchronized
            get() {
                if (_instance == null) {
                    _instance = TWBaseDatabase.getInstance(TWCacheDatabase::class.java)
                }
                return _instance!!
            }
    }

    abstract fun tweetDao(): TweetDao
    abstract fun tweetAccountDao(): TweetAccountDao
    abstract fun tweetSourceDao(): TweetSourceDao
    abstract fun userDao(): UserDao
}
