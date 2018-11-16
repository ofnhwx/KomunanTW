package net.komunan.komunantw.common.extension

import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources
import android.net.Uri
import androidx.annotation.AnyRes
import net.komunan.komunantw.TWContext

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

val Context.uri get() = ResourceMapper {
    Uri.Builder().scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(resources.getResourcePackageName(it))
            .path("${resources.getResourceTypeName(it)}/${resources.getResourceEntryName(it)}")
            .build()
}

val uri get() = TWContext.uri
