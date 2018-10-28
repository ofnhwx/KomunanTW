package net.komunan.komunantw.common

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import net.komunan.komunantw.ReleaseApplication

fun Int.string(): String = ReleaseApplication.context.getString(this)
fun Int.toBoolean(): Boolean = this != 0

fun Boolean.toInt(): Int = if (this) 1 else 0

fun <T> LiveData<T>.observeOnNotNull(owner: LifecycleOwner, body: (data: T) -> Unit) {
    this.observe(owner, Observer { it?.let(body) })
}
