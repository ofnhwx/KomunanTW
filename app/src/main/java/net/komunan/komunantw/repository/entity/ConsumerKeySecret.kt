package net.komunan.komunantw.repository.entity

import android.arch.persistence.room.*
import net.komunan.komunantw.BuildConfig
import net.komunan.komunantw.R
import net.komunan.komunantw.common.string
import net.komunan.komunantw.repository.database.TWDatabase

@Entity
class ConsumerKeySecret {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var name: String = ""
        get() = if (default) R.string.default_label.string() else field
    @ColumnInfo(name = "consumer_key")
    var consumerKey: String = ""
        get() = if (default) BuildConfig.DEFAULT_CONSUMER_KEY else field
    @ColumnInfo(name = "consumer_secret")
    var consumerSecret: String = ""
        get() = if (default) BuildConfig.DEFAULT_CONSUMER_SECRET else field
    @ColumnInfo(name = "is_default")
    var defaultKey: Int = 0

    var default: Boolean
        get() = defaultKey != 0
        set(value) {
            defaultKey = if (value) 1 else 0
        }

    fun save() = TWDatabase.instance.consumerKeySecretDao().save(this)

    fun delete() {
        if (default) {
            return
        }
        TWDatabase.instance.consumerKeySecretDao().delete(this)
    }
}
