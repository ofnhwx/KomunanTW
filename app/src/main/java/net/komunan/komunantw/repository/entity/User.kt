package net.komunan.komunantw.repository.entity

import androidx.room.*
import net.komunan.komunantw.repository.database.TWCacheDatabase
import twitter4j.User as TwitterUser

@Entity(tableName = "user")
class User() {
    @PrimaryKey
    @ColumnInfo(name = "id")          var id        : Long = 0L
    @ColumnInfo(name = "image_url")   var imageUrl  : String = ""
    @ColumnInfo(name = "name")        var name      : String = ""
    @ColumnInfo(name = "screen_name") var screenName: String = ""
    @ColumnInfo(name = "cache_at")    var cacheAt   : Long = 0L

    companion object {
        private val dao = TWCacheDatabase.instance.userDao()

        @JvmStatic fun find(id: Long) = dao.find(id)
        @JvmStatic fun save(users: List<User>) = dao.save(users)
    }

    @Ignore
    constructor(user: TwitterUser): this() {
        this.id = user.id
        this.imageUrl = user.profileImageURLHttps
        this.name = user.name
        this.screenName = user.screenName
        this.cacheAt = System.currentTimeMillis()
    }

    override fun toString(): String {
        return "${User::class.simpleName}{ " +
                "id=$id, " +
                "imageUrl=$imageUrl, " +
                "name=$name, " +
                "screenName=$screenName," +
                "cacheAt=$cacheAt }"
    }
}
