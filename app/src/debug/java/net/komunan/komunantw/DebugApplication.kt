package net.komunan.komunantw

import android.os.StrictMode
import com.facebook.stetho.Stetho
import com.github.ajalt.timberkt.Timber
import io.objectbox.android.AndroidObjectBrowser
import net.komunan.komunantw.core.repository.ObjectBox

class DebugApplication : ReleaseApplication() {
    override fun onCreate() {
        super.onCreate()
        AndroidObjectBrowser(ObjectBox.get()).start(this)
        withStrictLog { Stetho.initializeWithDefaults(this) }
    }

    override fun timberTree() = Timber.DebugTree()

    private fun withStrictLog(f: () -> Unit) {
        StrictMode.getThreadPolicy().let { policy ->
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build())
            f()
            StrictMode.setThreadPolicy(policy)
        }
    }
}
