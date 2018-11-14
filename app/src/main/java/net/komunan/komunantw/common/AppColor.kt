package net.komunan.komunantw.common

import android.graphics.Color

@Suppress("unused", "SpellCheckingInspection", "MemberVisibilityCanBePrivate")
object AppColor {
    const val RED     = Color.RED
    const val GREEN   = Color.GREEN
    const val BLUE    = Color.BLUE
    const val CYAN    = Color.CYAN
    const val MAGENTA = Color.MAGENTA
    const val YELLOW  = Color.YELLOW
    const val WHITE   = Color.WHITE
    const val BLACK   = Color.BLACK
    const val GRAY    = Color.GRAY
    const val DKGRAY  = Color.DKGRAY
    const val LTGRAY  = Color.LTGRAY
    const val TRANSPARENT = Color.TRANSPARENT

    val ORANGE    = Color.parseColor("#FFA500")
    val HOLO_BLUE = Color.parseColor("#33B5E5")

    val LINK         = HOLO_BLUE
    val LINK_PRESSED = HOLO_BLUE

    const val RETWEETED = GREEN
    const val LIKED     = RED
}
