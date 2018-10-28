package net.komunan.komunantw.repository.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
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
    constructor(user: User): this(user.id, user.profileImageURLHttps, user.name, user.screenName, 0, 0)

    fun save() = dao.save(this)
    fun delete() = dao.delete(this)
    fun credential() = Credential.findByAccountId(id).first()
    fun sources() = Source.findByAccountId(id)
}
