package net.komunan.komunantw.repository.database

import androidx.room.Room
import androidx.room.RoomDatabase
import net.komunan.komunantw.Preference
import net.komunan.komunantw.TWContext

abstract class TWBaseDatabase: RoomDatabase() {
    internal companion object {
        fun <T: RoomDatabase> getInstance(clazz: Class<T>): T {
            val builder = if (Preference.useInMemoryDatabase) {
                Room.inMemoryDatabaseBuilder(TWContext, clazz)
            } else {
                Room.databaseBuilder(TWContext, clazz, clazz.simpleName)
            }
            return builder
                    //.allowMainThreadQueries()
                    .build()
        }
    }
}

enum class TransactionTarget {
    NORMAL,
    WITH_CACHE,
    CACHE_ONLY,
}

fun transaction(target: TransactionTarget = TransactionTarget.NORMAL, body: () -> Unit) {
    val databases = when (target) {
        TransactionTarget.NORMAL -> listOf(TWDatabase.instance)
        TransactionTarget.WITH_CACHE -> listOf(TWDatabase.instance, TWCacheDatabase.instance)
        TransactionTarget.CACHE_ONLY -> listOf(TWCacheDatabase.instance)
    }
    databases.forEach { it.beginTransaction() }
    try {
        val result = body()
        databases.forEach { it.setTransactionSuccessful() }
        return result
    } finally {
        databases.forEach { it.endTransaction() }
    }
}
