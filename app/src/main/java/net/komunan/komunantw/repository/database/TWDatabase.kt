package net.komunan.komunantw.repository.database

import android.arch.persistence.room.*
import net.komunan.komunantw.Preference
import net.komunan.komunantw.ReleaseApplication
import net.komunan.komunantw.repository.dao.*
import net.komunan.komunantw.repository.entity.*

@Database(
        entities = [
            Account::class,
            Column::class,
            ColumnSource::class,
            ConsumerKeySecret::class,
            Credential::class,
            Source::class,
            Tweet::class
        ],
        version = 1,
        exportSchema = false
)
abstract class TWDatabase: RoomDatabase() {
    companion object {
        private var instance_: TWDatabase? = null

        @JvmStatic
        val instance: TWDatabase
            get() {
                if (instance_ == null) {
                    val builder = if (Preference.useInMemoryDatabase) {
                        Room.inMemoryDatabaseBuilder(ReleaseApplication.context, TWDatabase::class.java)
                    } else {
                        Room.databaseBuilder(ReleaseApplication.context, TWDatabase::class.java, TWDatabase::class.java.simpleName)
                    }
                    instance_ = builder
                            .allowMainThreadQueries()
                            .build()
                }
                return instance_!!
            }
    }

    abstract fun accountDao(): AccountDao
    abstract fun columnDao(): ColumnDao
    abstract fun columnSourceDao(): ColumnSourceDao
    abstract fun consumerKeySecretDao(): ConsumerKeySecretDao
    abstract fun credentialDao(): CredentialDao
    abstract fun sourceDao(): SourceDao
    abstract fun tweetDao(): TweetDao
}
