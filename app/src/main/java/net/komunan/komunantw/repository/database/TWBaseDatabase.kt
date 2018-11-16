package net.komunan.komunantw.repository.database

import androidx.room.Room
import androidx.room.RoomDatabase
import net.komunan.komunantw.common.Preference
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
