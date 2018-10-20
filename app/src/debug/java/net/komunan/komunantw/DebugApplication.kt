package net.komunan.komunantw

import android.os.StrictMode
import com.facebook.stetho.Stetho

class DebugApplication: ReleaseApplication() {
    override fun onCreate() {
        super.onCreate()
        withStrictLog { Stetho.initializeWithDefaults(this) }
    }

    private fun withStrictLog(f: () -> Unit) {
        StrictMode.getThreadPolicy().let { policy ->
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build())
            f()
            StrictMode.setThreadPolicy(policy)
        }
    }
}
