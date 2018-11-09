package net.komunan.komunantw.repository.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import net.komunan.komunantw.R
import net.komunan.komunantw.extension.string
import net.komunan.komunantw.extension.uri
import net.komunan.komunantw.repository.database.TWCacheDatabase
import twitter4j.Status
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
        @JvmStatic fun dummy(): User = User().apply {
            imageUrl = uri[R.mipmap.ic_launcher].toString()
            name = string[R.string.dummy]()
            screenName = string[R.string.dummy]()
            dummy = true
        }

        @JvmStatic
        fun createCache(statuses: List<Status>) {
            val users1 = statuses.map { User(it.user) }
            val users2 = statuses.mapNotNull { it.retweetedStatus }.map { User(it.user) }
            dao.save(users1.plus(users2).distinctBy { it.id }.filterNot { it.dummy })
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
