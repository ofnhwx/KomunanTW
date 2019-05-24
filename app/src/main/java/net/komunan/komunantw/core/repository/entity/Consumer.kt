package net.komunan.komunantw.core.repository.entity

import io.objectbox.Box
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.kotlin.boxFor
import io.objectbox.query.QueryBuilder
import net.komunan.komunantw.BuildConfig
import net.komunan.komunantw.R
import net.komunan.komunantw.common.string
import net.komunan.komunantw.core.repository.ObjectBox

@Entity
class Consumer() {
    @Id
    var id: Long = 0L
    var name: String = ""
    var key: String = ""
    var secret: String = ""
    var isDefault: Boolean = false
    var savedAt: Long = 0L

    companion object {
        @JvmStatic
        val box: Box<Consumer>
            get() = ObjectBox.get().boxFor(Consumer::class)

        @JvmStatic
        fun query(): QueryBuilder<Account> = Account.box.query()

        @JvmStatic
        val default: Consumer
            get() {
                val consumer = box.query()
                        .equal(Consumer_.isDefault, true)
                        .build()
                        .findUnique()
                return consumer ?: Consumer().apply {
                    this.name = string[R.string.defaults]()
                    this.key = BuildConfig.DEFAULT_CONSUMER_KEY
                    this.secret = BuildConfig.DEFAULT_CONSUMER_SECRET
                    this.isDefault = true
                }.save()
            }
    }

    constructor(name: String, key: String, secret: String) : this() {
        this.name = name
        this.key = key
        this.secret = secret
    }

    fun save(): Consumer {
        box.put(apply {
            savedAt = System.currentTimeMillis()
        })
        return this
    }

    override fun equals(other: Any?): Boolean {
        return other is Consumer
                && this.id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
