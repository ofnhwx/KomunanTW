package net.komunan.komunantw.repository.entity

import android.arch.persistence.room.*
import net.komunan.komunantw.repository.database.TWDatabase

@Entity
class Column {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var name: String = ""
    var position: Int = 0

    fun save() {
        val dao = TWDatabase.instance.columnDao()
        if (id == 0L) {
            position = dao.count() + 1
        }
        dao.save(this)
    }

    fun delete() {
        TWDatabase.instance.let { db ->
            db.beginTransaction()
            try {
                db.columnDao().let { dao ->
                    dao.packPosition(position)
                    dao.delete(this)
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    fun moveTo(position: Int) {
        TWDatabase.instance.let { db ->
            db.beginTransaction()
            try {
                db.columnDao().let { dao ->
                    when {
                        position > this.position -> dao.shiftLeft(this.position + 1, position)
                        position < this.position -> dao.shiftRight(position, this.position - 1)
                    }
                }
                this.position = position
                this.save()
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }
}
