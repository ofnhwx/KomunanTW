package net.komunan.komunantw.repository.database

import androidx.room.Database
import androidx.room.RoomDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.komunan.komunantw.repository.dao.*
import net.komunan.komunantw.repository.entity.*

@Database(
        entities = [
            Account::class,
            Timeline::class,
            TimelineSource::class,
            ConsumerKeySecret::class,
            Credential::class,
            Source::class
        ],
        version = 1,
        exportSchema = false
)
abstract class TWDatabase: RoomDatabase() {
    @Suppress("ObjectPropertyName")
    companion object {
        private var _instance: TWDatabase? = null

        @JvmStatic
        val instance: TWDatabase
            @Synchronized
            get() {
                if (_instance == null) {
                    _instance = TWBaseDatabase.getInstance(TWDatabase::class.java)
                    GlobalScope.launch {
                        // 標準のAPIキーを登録
                        if (ConsumerKeySecret.count() == 0) {
                            ConsumerKeySecret.default()
                        }
                    }
                }
                return _instance!!
            }
    }

    abstract fun accountDao(): AccountDao
    abstract fun consumerKeySecretDao(): ConsumerKeySecretDao
    abstract fun credentialDao(): CredentialDao
    abstract fun sourceDao(): SourceDao
    abstract fun timelineDao(): TimelineDao
    abstract fun timelineSourceDao(): TimelineSourceDao
}
