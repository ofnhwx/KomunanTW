package net.komunan.komunantw.repository.entity

import androidx.room.*
import net.komunan.komunantw.BuildConfig
import net.komunan.komunantw.R
import net.komunan.komunantw.string
import net.komunan.komunantw.toBoolean
import net.komunan.komunantw.toInt
import net.komunan.komunantw.repository.database.TWDatabase

@Entity(tableName = "consumer_key_secret")
data class ConsumerKeySecret (
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        var id: Long,
        @ColumnInfo(name = "name")
        var _name: String,
        @ColumnInfo(name = "consumer_key")
        var _consumerKey: String,
        @ColumnInfo(name = "consumer_secret")
        var _consumerSecret: String,
        @ColumnInfo(name = "is_default")
        var _default: Int,
        @ColumnInfo(name = "create_at")
        var createAt: Long,
        @ColumnInfo(name = "update_at")
        var updateAt: Long
) {
    companion object {
        private val dao = TWDatabase.instance.consumerKeySecretDao()

        @JvmStatic fun findAllAsync() = dao.findAllAsync()
        @JvmStatic fun findAll() = dao.findAll()
        @JvmStatic fun count()= dao.count()
        @JvmStatic fun default() = ConsumerKeySecret("", "", "").apply { default = true }
    }

    var name: String
        @Ignore
        get() = if (default) R.string.default_label.string() else _name
        set(value) {
            if (!default) {
                _name = value
            }
        }

    var consumerKey: String
        @Ignore
        get() = if (default) BuildConfig.DEFAULT_CONSUMER_KEY else _consumerKey
        set(value) {
            if (!default) {
                _consumerKey = value
            }
        }

    var consumerSecret: String
        @Ignore
        get() = if (default) BuildConfig.DEFAULT_CONSUMER_SECRET else _consumerSecret
        set(value) {
            if (!default) {
                _consumerSecret = value
            }
        }

    var default: Boolean
        get() = _default.toBoolean()
        set(value) {
            _default = value.toInt()
            if (value) {
                _name = ""
                _consumerKey = ""
                _consumerSecret = ""
            }
        }

    @Ignore
    constructor(name: String, consumerKey: String, consumerSecret: String)
            : this(0, name, consumerKey, consumerSecret, 0, 0, 0)

    fun save() = dao.save(this)
    fun delete() = dao.delete(this)
}
