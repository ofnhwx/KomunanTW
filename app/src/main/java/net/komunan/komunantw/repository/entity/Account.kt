package net.komunan.komunantw.repository.entity

import androidx.room.*
import net.komunan.komunantw.common.Diffable
import net.komunan.komunantw.repository.database.TWDatabase
import twitter4j.User

@Entity(tableName = "account")
class Account(): Diffable {
    @PrimaryKey
    @ColumnInfo(name = "id")          var id        : Long = 0L
    @ColumnInfo(name = "image_url")   var imageUrl  : String = ""
    @ColumnInfo(name = "name")        var name      : String = ""
    @ColumnInfo(name = "screen_name") var screenName: String = ""
    @ColumnInfo(name = "create_at")   var createAt  : Long = 0L
    @ColumnInfo(name = "update_at")   var updateAt  : Long = 0L

    companion object {
        private val dao = TWDatabase.instance.accountDao()

        @JvmStatic fun findAllAsync() = dao.findAllAsync()
        @JvmStatic fun find(id: Long) = dao.find(id)
        @JvmStatic fun count() = dao.count()
    }

    @Ignore
    constructor(user: User): this() {
        this.id = user.id
        this.imageUrl = user.profileImageURLHttps
        this.name = user.name
        this.screenName = user.screenName
    }

    fun save() = dao.save(this)
    fun delete() = dao.delete(this)
    fun credential() = Credential.findByAccount(this).first()
    fun sources() = Source.findByAccount(this)

    override fun toString(): String {
        return "${Account::class.simpleName}{ " +
                "id=$id, " +
                "imageUrl=$imageUrl, " +
                "name=$name, " +
                "screenName=$screenName, " +
                "createAt=$createAt, " +
                "updateAt=$updateAt }"
    }

    override fun isTheSame(other: Diffable): Boolean {
        return other is Account
                && this.id == other.id
    }

    override fun isContentsTheSame(other: Diffable): Boolean {
        return other is Account
                && this.id == other.id
                && this.imageUrl == other.imageUrl
                && this.name == other.name
                && this.screenName == other.screenName
    }
}
