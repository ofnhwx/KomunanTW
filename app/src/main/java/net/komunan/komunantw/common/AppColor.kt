package net.komunan.komunantw.common

import android.graphics.Color

@Suppress("unused", "SpellCheckingInspection", "MemberVisibilityCanBePrivate")
object AppColor {
    const val RED = Color.RED
    const val GREEN = Color.GREEN
    const val BLUE = Color.BLUE
    const val CYAN = Color.CYAN
    const val MAGENTA = Color.MAGENTA
    const val YELLOW = Color.YELLOW
    const val WHITE = Color.WHITE
    const val BLACK = Color.BLACK
    const val GRAY = Color.GRAY
    const val DKGRAY = Color.DKGRAY
    const val LTGRAY = Color.LTGRAY
    const val TRANSPARENT = Color.TRANSPARENT

    val HOLO_BLUE = Color.parseColor("#33b5e5")

    val LINK by lazy { HOLO_BLUE }
    val LINK_PRESSED by lazy { HOLO_BLUE }

    @Suppress("FunctionName") fun RETWEETED(retweeted: Boolean) = if (retweeted) GREEN else GRAY
    @Suppress("FunctionName") fun LIKED(liked: Boolean) = if (liked) RED else GRAY
}
