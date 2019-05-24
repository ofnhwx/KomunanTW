package net.komunan.komunantw.common

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import androidx.annotation.AnyRes
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonSerializer
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import net.komunan.komunantw.TWContext
import java.util.*

class ResourceMapper<out T>(private val mapRes: (resId: Int) -> T) {
    operator fun get(@AnyRes resId: Int) = mapRes(resId)
}

class FormattedString(private val resources: Resources, private val resId: Int) {
    operator fun invoke(vararg values: Any): String = resources.getString(resId, *values)
    operator fun invoke(quantity: Int): String = resources.getQuantityString(resId, quantity)
    operator fun invoke(quantity: Int, vararg values: Any): String = resources.getQuantityString(resId, quantity, *values)
}

val Context.string get() = ResourceMapper { FormattedString(resources, it) }

val string get() = TWContext.string

val Context.uri
    get() = ResourceMapper {
        Uri.Builder().scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(resources.getResourcePackageName(it))
                .path("${resources.getResourceTypeName(it)}/${resources.getResourceEntryName(it)}")
                .build()
    }

val uri get() = TWContext.uri

val Context.navigationBarHeight: Int
    get() {
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }

fun Int.dp(): Int {
    return (TWContext.resources.displayMetrics.density * this).toInt()
}

fun Int.sp(): Int {
    return (TWContext.resources.displayMetrics.scaledDensity * this).toInt()
}

fun Long.commaSeparated(): String {
    return "%,d".format(this)
}

fun String.openUrl() {
    return TWContext.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(this)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    })
}

fun IIcon.make(context: Context): IconicsDrawable {
    return IconicsDrawable(context).icon(this)
}

fun WorkManager.enqueueSequentially(name: String, policy: ExistingWorkPolicy, requests: List<OneTimeWorkRequest>): List<UUID> {
    return if (requests.any()) {
        var continuous = beginUniqueWork(name, policy, requests.first())
        for (request in requests.drop(1)) {
            continuous = continuous.then(request)
        }
        continuous.enqueue()
        requests.map(WorkRequest::getId)
    } else {
        emptyList()
    }
}

fun PrimaryDrawerItem.withStringRes(@StringRes stringRes: Int): PrimaryDrawerItem {
    return this.withIdentifier(stringRes.toLong()).withName(stringRes)
}

fun SecondaryDrawerItem.withStringRes(@StringRes stringRes: Int): SecondaryDrawerItem {
    return this.withIdentifier(stringRes.toLong()).withName(stringRes)
}

val gson: Gson = GsonBuilder().registerTypeAdapter(List::class.java, JsonSerializer<List<*>> { src, _, context ->
    if (src?.isEmpty() != false) {
        return@JsonSerializer null
    }
    val result = JsonArray()
    src.map { context.serialize(it) }.forEach { result.add(it) }
    return@JsonSerializer result
}).create()

fun <T1, T2, R> combineLatest(source1: LiveData<T1>, source2: LiveData<T2>, func: (T1?, T2?) -> R?): LiveData<R> {
    val result = MediatorLiveData<R>()
    result.addSource(source1) { result.value = func.invoke(source1.value, source2.value) }
    result.addSource(source2) { result.value = func.invoke(source1.value, source2.value) }
    return result
}
