package net.komunan.komunantw

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.facebook.drawee.backends.pipeline.Fresco
import com.github.ajalt.timberkt.Timber
import com.marcinmoskala.kotlinpreferences.PreferenceHolder
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.Iconics
import net.komunan.komunantw.core.repository.ObjectBox

@SuppressLint("Registered")
open class ReleaseApplication : Application() {
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
        Iconics.registerFont(GoogleMaterial())
        ObjectBox.init(TWContext)
        Fresco.initialize(TWContext)
        PreferenceHolder.setContext(TWContext)
        Timber.plant(timberTree())
    }

    protected open fun timberTree() = Timber.DebugTree()
}

val TWApplication: ReleaseApplication
    get() = ReleaseApplication.instance

val TWContext: Context
    get() = ReleaseApplication.context
