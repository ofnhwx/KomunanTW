package net.komunan.komunantw.extension

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.google.gson.Gson
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import net.komunan.komunantw.TWContext
import net.komunan.komunantw.repository.database.TWCacheDatabase
import net.komunan.komunantw.repository.database.TWDatabase
import java.util.*

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

// WorkManager
fun WorkManager.enqueueSequentilly(name: String, policy: ExistingWorkPolicy, requests: List<OneTimeWorkRequest>): List<UUID> {
    return if (requests.any()) {
        var continuous = WorkManager.getInstance().beginUniqueWork(name, policy, requests.first())
        for (request in requests.drop(1)) {
            continuous = continuous.then(request)
        }
        continuous.enqueue()
        requests.map(WorkRequest::getId)
    } else {
        emptyList()
    }
}

// Global
val gson = Gson()

fun <T1, T2, R> combineLatest(source1: LiveData<T1>, source2: LiveData<T2>, func: (T1?, T2?) -> R?): LiveData<R> {
    val result = MediatorLiveData<R>()
    result.addSource(source1) { result.value = func.invoke(source1.value, source2.value) }
    result.addSource(source2) { result.value = func.invoke(source1.value, source2.value) }
    return result
}

enum class TransactionTarget {
    NORMAL,
    WITH_CACHE,
    CACHE_ONLY,
}

fun transaction(target: TransactionTarget = TransactionTarget.NORMAL, body: () -> Unit) {
    val databases = when (target) {
        TransactionTarget.NORMAL -> listOf(TWDatabase.instance)
        TransactionTarget.WITH_CACHE -> listOf(TWDatabase.instance, TWCacheDatabase.instance)
        TransactionTarget.CACHE_ONLY -> listOf(TWCacheDatabase.instance)
    }
    databases.forEach { it.beginTransaction() }
    try {
        val result = body()
        databases.forEach { it.setTransactionSuccessful() }
        return result
    } finally {
        databases.forEach { it.endTransaction() }
    }
}
