package net.komunan.komunantw

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.facebook.drawee.backends.pipeline.Fresco
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
        Fresco.initialize(context)
        PreferenceHolder.setContext(context)
        Timber.plant(timberTree())
    }

    protected open fun timberTree() = Timber.DebugTree()
}
