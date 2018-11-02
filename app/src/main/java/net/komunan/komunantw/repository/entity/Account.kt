package net.komunan.komunantw.repository.entity

import androidx.room.*
import net.komunan.komunantw.repository.database.TWDatabase
import twitter4j.User

@Entity(tableName = "account")
class Account(
        @PrimaryKey()
        @ColumnInfo(name = "id")
        var id: Long,
        @ColumnInfo(name = "image_url")
        var imageUrl: String,
        @ColumnInfo(name = "name")
        var name: String,
        @ColumnInfo(name = "screen_name")
        var screenName: String,
        @ColumnInfo(name = "create_at")
        var createAt: Long,
        @ColumnInfo(name = "update_at")
        var updateAt: Long
) {
    companion object {
        private val dao = TWDatabase.instance.accountDao()

        @JvmStatic fun findAllAsync() = dao.findAllAsync()
        @JvmStatic fun find(id: Long) = dao.find(id)
        @JvmStatic fun count() = dao.count()
    }

    @Ignore
    constructor(user: User): this(
            id = user.id,
            imageUrl = user.profileImageURLHttps,
            name = user.name,
            screenName = user.screenName,
            createAt = 0,
            updateAt = 0
    )

    fun save() = dao.save(this)
    fun delete() = dao.delete(this)
    fun credential() = Credential.findByAccount(this).first()
    fun sources() = Source.findByAccount(this)
}
