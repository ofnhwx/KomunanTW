package net.komunan.komunantw.common

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.view.ViewManager
import com.facebook.drawee.view.SimpleDraweeView
import net.komunan.komunantw.ReleaseApplication
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
