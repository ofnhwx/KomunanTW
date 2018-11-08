package net.komunan.komunantw.common

import android.graphics.Color

object AppColor {
    val RED = Color.RED
    val GREEN = Color.GREEN
    val BLUE = Color.BLUE
    val CYAN = Color.CYAN
    val MAGENTA = Color.MAGENTA
    val YELLOW = Color.YELLOW
    val WHITE = Color.WHITE
    val BLACK = Color.BLACK
    val GRAY = Color.GRAY
    val DKGRAY = Color.DKGRAY
    val LTGRAY = Color.LTGRAY
    val TRANSPARENT = Color.TRANSPARENT

    val HOLO_BLUE = Color.parseColor("#33b5e5")

    val LINK by lazy { HOLO_BLUE }
    val LINK_PRESSED by lazy { HOLO_BLUE }

    fun RETWEETED(retweeted: Boolean) = if (retweeted) GREEN else GRAY
    fun LIKED(liked: Boolean) = if (liked) RED else GRAY
}
