package net.komunan.komunantw.repository.entity

import android.arch.persistence.room.*
import net.komunan.komunantw.repository.database.TWDatabase

@Entity(
        foreignKeys = [
            ForeignKey(
                    entity = Account::class,
                    parentColumns = ["id"],
                    childColumns = ["account_id"],
                    onDelete = ForeignKey.CASCADE,
                    onUpdate = ForeignKey.CASCADE,
                    deferred = true
            )
        ]
)
open class Source {
    enum class Type {
        HOME,
        MENTION,
        SEARCH,
    }

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    @ColumnInfo(name = "account_id", index = true)
    var accountId: Long = 0
    var type: Int = 0
    var query: String? = null

    fun save() = TWDatabase.instance.sourceDao().save(this)
    fun delete() = TWDatabase.instance.sourceDao().delete(this)
}

class SourceWithAccount: Source() {
    @Relation(entity = Tweet::class, parentColumn = "account_id", entityColumn = "id")
    var accounts: List<Account> = emptyList()

    val account: Account
        get() = accounts[0]
}
