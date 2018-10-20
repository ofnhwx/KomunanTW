package net.komunan.komunantw.repository.entity

import android.arch.persistence.room.*
import net.komunan.komunantw.repository.database.TWDatabase

@Entity
open class Account {
    @PrimaryKey
    var id: Long = 0
    var name: String = ""
    @ColumnInfo(name = "screen_name")
    var screenName: String = ""

    open fun save() = TWDatabase.instance.accountDao().save(this)
    open fun delete() = TWDatabase.instance.accountDao().delete(this)
}

class AccountWithCredential: Account() {
    @Relation(entity = Credential::class, parentColumn = "id", entityColumn = "account_id")
    var credentials: List<Credential> = emptyList()
}
