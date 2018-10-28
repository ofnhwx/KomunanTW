package net.komunan.komunantw.ui.activity

import android.support.v7.widget.Toolbar
import android.view.View
import net.komunan.komunantw.R
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.toolbar

internal class TWActivityUI: AnkoComponent<TWActivity> {
    lateinit var toolbar: Toolbar
    lateinit var container: View

    override fun createView(ui: AnkoContext<TWActivity>) = with(ui) {
        verticalLayout {
            toolbar = toolbar {
                id = R.id.toolbar
            }.lparams(matchParent, wrapContent)
            container = frameLayout {
                id = R.id.container
            }.lparams(matchParent, dip(0), 1.0f)
        }
    }
}
