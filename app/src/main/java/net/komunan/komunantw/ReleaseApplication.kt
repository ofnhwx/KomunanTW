package net.komunan.komunantw

import android.app.Application
import android.content.Context
import com.marcinmoskala.kotlinpreferences.PreferenceHolder
import net.komunan.komunantw.repository.database.TWDatabase
import net.komunan.komunantw.repository.entity.ConsumerKeySecret

open class ReleaseApplication: Application() {
    companion object {
        @JvmStatic
        lateinit var instance: ReleaseApplication
            private set

        @JvmStatic
        val context: Context
            get() = instance
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        PreferenceHolder.setContext(this)

        // アプリケーションの起動時にデフォルトの ConsumerKey/Secret を登録
        TWDatabase.instance.consumerKeySecretDao().run {
            if (count() == 0) {
                save(ConsumerKeySecret().apply {
                    default = true
                })
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
    }
}
