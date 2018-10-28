package net.komunan.komunantw.repository.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import net.komunan.komunantw.repository.database.TWCacheDatabase
import twitter4j.User as TwitterUser

@Entity(tableName = "user")
data class User(
        @PrimaryKey
        var id: Long,
        @ColumnInfo(name = "name")
        var name: String,
        @ColumnInfo(name = "screen_name")
        var screenName: String,
        @ColumnInfo(name = "cache_at")
        var cacheAt: Long
) {
    companion object {
        private fun dao() = TWCacheDatabase.instance.userDao()

        @JvmStatic
        fun find(id: Long) = dao().find(id)

        @JvmStatic
        fun save(users: Collection<User>) = dao().save(users)
    }

    @Ignore
    constructor(user: TwitterUser): this(user.id, user.name, user.screenName, System.currentTimeMillis())
}