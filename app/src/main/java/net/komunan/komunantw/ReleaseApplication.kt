package net.komunan.komunantw

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.github.ajalt.timberkt.Timber
import com.marcinmoskala.kotlinpreferences.PreferenceHolder

@SuppressLint("Registered")
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
        PreferenceHolder.setContext(context)
        Timber.plant(timberTree())
    }

    protected open fun timberTree() = Timber.asTree()
}
