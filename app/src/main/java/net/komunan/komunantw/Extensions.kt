package net.komunan.komunantw

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon

// @DrawableRes
fun Int.uri(): Uri {
    return Uri.Builder().also { builder ->
        builder.scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
        ReleaseApplication.context.resources.also { resources ->
            builder.authority(resources.getResourcePackageName(this))
            builder.path("${resources.getResourceTypeName(this)}/${resources.getResourceEntryName(this)}")
        }
    }.build()
}

// @StringRes
fun Int.string(): String = ReleaseApplication.context.getString(this)
fun Int.string(arg: String?): String = ReleaseApplication.context.getString(this, arg)
fun Int.string(arg1: String?, arg2: String?): String = ReleaseApplication.context.getString(this, arg1, arg2)
fun Int.string(arg1: String?, arg2: String?, arg3: String?): String = ReleaseApplication.context.getString(this, arg1, arg2, arg3)

// Int
fun Int.toBoolean(): Boolean = this != 0
fun Int.dp(): Float {
    return ReleaseApplication.context.resources.displayMetrics.density * this
}
fun Int.sp(): Float {
    return ReleaseApplication.context.resources.displayMetrics.scaledDensity * this
}

// Boolean
fun Boolean.toInt(): Int = if (this) 1 else 0

// IIcon
fun IIcon.make(context: Context): IconicsDrawable {
    return IconicsDrawable(context).icon(this)
}

// LiveData<T>
fun <T> LiveData<T>.observeOnNotNull(owner: LifecycleOwner, body: (data: T) -> Unit) {
    this.observe(owner, Observer { it?.let(body) })
}

// Global
fun <T1, T2, R> combineLatest(source1: LiveData<T1>, source2: LiveData<T2>, func: (T1?, T2?) -> R?): LiveData<R> {
    val result = MediatorLiveData<R>()
    result.addSource(source1) { result.value = func.invoke(source1.value, source2.value) }
    result.addSource(source2) { result.value = func.invoke(source1.value, source2.value) }
    return result
}
