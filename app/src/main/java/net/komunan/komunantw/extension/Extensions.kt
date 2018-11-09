package net.komunan.komunantw.extension

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import net.komunan.komunantw.TWContext

// Int
fun Int.toBoolean(): Boolean = this != 0
fun Int.dp(): Float {
    return TWContext.resources.displayMetrics.density * this
}
fun Int.sp(): Float {
    return TWContext.resources.displayMetrics.scaledDensity * this
}

// Boolean
fun Boolean.toInt(): Int = if (this) 1 else 0

// String
fun String.uri(): Uri = Uri.parse(this)
fun String.intentActionView() = TWContext.startActivity(Intent(Intent.ACTION_VIEW, this.uri()))

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
