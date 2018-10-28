package net.komunan.komunantw.ui.auth

import android.widget.Button
import android.widget.EditText
import net.komunan.komunantw.R
import net.komunan.komunantw.common.string
import org.jetbrains.anko.*

internal class AuthUI: AnkoComponent<AuthActivity> {
    lateinit var openBrowser: Button
    lateinit var pin: EditText
    lateinit var submit: Button

    override fun createView(ui: AnkoContext<AuthActivity>) = with(ui) {
        verticalLayout {
            linearLayout {
                pin = editText {
                    hint = R.string.pin.string()
                }.lparams(matchParent, wrapContent)
            }.lparams(matchParent, dip(0), 1.0f)
            linearLayout {
                openBrowser = button(R.string.open_browser).lparams(dip(0), matchParent, 1.0f)
                submit = button(R.string.authentication).lparams(dip(0), matchParent, 1.0f)
            }.lparams(matchParent, wrapContent)
        }
    }
}
