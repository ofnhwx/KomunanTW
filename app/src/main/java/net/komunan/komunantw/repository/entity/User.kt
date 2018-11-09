package net.komunan.komunantw.repository.entity

import androidx.room.*
import net.komunan.komunantw.repository.database.TWCacheDatabase
import net.komunan.komunantw.R
import net.komunan.komunantw.extension.string
import net.komunan.komunantw.extension.uri
import twitter4j.User as TwitterUser

@Entity(tableName = "user")
class User() {
    @PrimaryKey
    @ColumnInfo(name = "id")          var id        : Long = 0L
    @ColumnInfo(name = "image_url")   var imageUrl  : String = ""
    @ColumnInfo(name = "name")        var name      : String = ""
    @ColumnInfo(name = "screen_name") var screenName: String = ""
    @ColumnInfo(name = "cache_at")    var cacheAt   : Long = 0L

    @Ignore
    var dummy = false

    companion object {
        private val dao = TWCacheDatabase.instance.userDao()

        @JvmStatic fun find(id: Long) = dao.find(id)
        @JvmStatic fun save(users: List<User>) = dao.save(users.filter { !it.dummy })
        @JvmStatic fun dummy(): User = User().apply {
            imageUrl = uri[R.mipmap.ic_launcher].toString()
            name = string[R.string.dummy]()
            screenName = string[R.string.dummy]()
            dummy = true
        }
    }

    @Ignore
    constructor(user: TwitterUser): this() {
        this.id = user.id
        this.imageUrl = user.biggerProfileImageURLHttps
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
