package net.komunan.komunantw.repository.entity

import androidx.room.*
import net.komunan.komunantw.BuildConfig
import net.komunan.komunantw.R
import net.komunan.komunantw.common.extension.string
import net.komunan.komunantw.repository.database.TWDatabase
import org.apache.commons.lang3.builder.ToStringBuilder

@Entity(tableName = "consumer")
class Consumer() {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")          var id        : Long = 0L
    @ColumnInfo(name = "name")        var name      : String = ""
    @ColumnInfo(name = "key")         var key       : String = ""
    @ColumnInfo(name = "secret")      var secret    : String = ""
    @ColumnInfo(name = "default_key") var defaultKey: Boolean = false
    @ColumnInfo(name = "create_at")   var createAt  : Long = 0L
    @ColumnInfo(name = "update_at")   var updateAt  : Long = 0L

    companion object {
        @JvmStatic
        val dao = TWDatabase.instance.consumerKeySecretDao()

        @JvmStatic
        fun default() = dao.findDefault() ?: Consumer().apply {
            this.name    = string[R.string.default_label]()
            this.key     = BuildConfig.DEFAULT_CONSUMER_KEY
            this.secret  = BuildConfig.DEFAULT_CONSUMER_SECRET
            this.defaultKey = true
        }.save()
    }

    @Ignore
    constructor(name: String, consumerKey: String, consumerSecret: String): this() {
        this.name = name
        this.key = consumerKey
        this.secret = consumerSecret
    }

    fun save() = dao.save(this)
    fun delete() = dao.delete(this)

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }
}
