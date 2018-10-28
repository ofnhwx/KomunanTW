package net.komunan.komunantw.ui.timeline

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.widget.TextView
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.textView

internal class TweetUI: AnkoComponent<ViewGroup> {
    lateinit var text: TextView

    @SuppressLint("ResourceType")
    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        textView {
            id = 1
            this@TweetUI.text = this
        }
    }
}
