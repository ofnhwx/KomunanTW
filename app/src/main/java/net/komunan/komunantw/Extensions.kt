package net.komunan.komunantw

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer


fun Int.string(): String = ReleaseApplication.context.getString(this)
fun Int.string(arg: String): String = ReleaseApplication.context.getString(this, arg)
fun Int.toBoolean(): Boolean = this != 0

fun Boolean.toInt(): Int = if (this) 1 else 0

fun <T> LiveData<T>.observeOnNotNull(owner: LifecycleOwner, body: (data: T) -> Unit) {
    this.observe(owner, Observer { it?.let(body) })
}

fun <T1, T2, R> combineLatest(source1: LiveData<T1>, source2: LiveData<T2>, func: (T1?, T2?) -> R?): LiveData<R> {
    val result = MediatorLiveData<R>()
    result.addSource(source1) { result.value = func.invoke(source1.value, source2.value) }
    result.addSource(source2) { result.value = func.invoke(source1.value, source2.value) }
    return result
}
