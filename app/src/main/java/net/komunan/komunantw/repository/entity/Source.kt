package net.komunan.komunantw.repository.entity

import androidx.room.*
import net.komunan.komunantw.R
import net.komunan.komunantw.common.Preference
import net.komunan.komunantw.common.extension.string
import net.komunan.komunantw.ui.common.base.Diffable
import net.komunan.komunantw.repository.database.TWDatabase
import org.apache.commons.lang3.builder.ToStringBuilder
import twitter4j.UserList

@Entity(
        tableName = "source",
        foreignKeys = [
            ForeignKey(
                    entity = Account::class,
                    parentColumns = ["id"],
                    childColumns = ["account_id"],
                    onDelete = ForeignKey.CASCADE,
                    onUpdate = ForeignKey.CASCADE,
                    deferred = true
            )
        ],
        indices = [
            Index("account_id")
        ]
)
open class Source(): Diffable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")         var id       : Long = 0
    @ColumnInfo(name = "account_id") var accountId: Long = 0
    @ColumnInfo(name = "ordinal")    var ordinal  : Int = 0
    @ColumnInfo(name = "type")       var type     : String = ""
    @ColumnInfo(name = "label")      var label    : String = ""
    @ColumnInfo(name = "query")      var query    : String? = null
    @ColumnInfo(name = "list_id")    var listId   : Long = 0
    @ColumnInfo(name = "list_owner") var listOwner: Long = 0
    @ColumnInfo(name = "fetch_at")   var fetchAt  : Long = 0
    @ColumnInfo(name = "create_at")  var createAt : Long = 0
    @ColumnInfo(name = "update_at")  var updateAt : Long = 0

    enum class Type(val standard: Boolean, val editable: Boolean) {
        HOME(true, false),
        MENTION(true, false),
        USER(true, false),
        LIKE(true, false),
        LIST(false, false),
        SEARCH(false, true),
    }

    companion object {
        @JvmStatic
        val dao = TWDatabase.instance.sourceDao()
    }

    @Ignore
    constructor(account: Account, type: Type): this() {
        this.accountId = account.id
        this.ordinal = type.ordinal
        this.type = type.toString()
    }

    @Ignore
    constructor(account: Account, userList: UserList): this(account, Type.LIST) {
        this.label = userList.name
        this.listOwner = account.id
        this.listId = userList.id
    }

    val displayName: String
        get() {
            return when (Type.valueOf(type)) {
                Type.HOME    -> string[R.string.home]()
                Type.MENTION -> string[R.string.mention]()
                Type.USER    -> string[R.string.user]()
                Type.LIKE    -> string[R.string.favorite]()
                Type.LIST    -> string[R.string.format_list_label](label)
                Type.SEARCH  -> string[R.string.format_search_label](label)
            }
        }

    fun save() = dao.save(this)
    fun delete() = dao.delete(this)
    fun requireAutoFetch() = System.currentTimeMillis() > (fetchAt + Preference.fetchIntervalMillis * Preference.fetchIntervalThreshold)

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }

    override fun isTheSame(other: Diffable): Boolean {
        return other is Source
                && this.id == other.id
    }

    override fun isContentsTheSame(other: Diffable): Boolean {
        return other is Source
                && this.id == other.id
                && this.accountId == other.accountId
                && this.ordinal == other.ordinal
                && this.type == other.type
                && this.label == other.label
                && this.query == other.query
                && this.listId == other.listId
                && this.listOwner == other.listOwner
    }
}
