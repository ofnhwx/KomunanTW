package net.komunan.komunantw

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.Observer
import android.view.ViewManager
import com.facebook.drawee.view.SimpleDraweeView
import org.jetbrains.anko.custom.ankoView

fun Int.string(): String = ReleaseApplication.context.getString(this)
fun Int.string(arg: String): String = ReleaseApplication.context.getString(this, arg)
fun Int.toBoolean(): Boolean = this != 0


fun Boolean.toInt(): Int = if (this) 1 else 0

fun <T> LiveData<T>.observeOnNotNull(owner: LifecycleOwner, body: (data: T) -> Unit) {
    this.observe(owner, Observer { it?.let(body) })
}

fun ViewManager.draweeView() = draweeView {}
fun ViewManager.draweeView(init: SimpleDraweeView.() -> Unit): SimpleDraweeView {
    return ankoView({ SimpleDraweeView(it) }, theme = 0, init = init)
}

fun <T1, T2, R> combineLatest(source1: LiveData<T1>, source2: LiveData<T2>, func: (T1?, T2?) -> R?): LiveData<R> {
    val result = MediatorLiveData<R>()
    result.addSource(source1) { result.value = func.invoke(source1.value, source2.value) }
    result.addSource(source2) { result.value = func.invoke(source1.value, source2.value) }
    return result
}
