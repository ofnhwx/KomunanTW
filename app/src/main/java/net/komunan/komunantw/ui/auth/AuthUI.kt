package net.komunan.komunantw.ui.auth

import android.support.constraint.ConstraintSet
import android.widget.Button
import android.widget.EditText
import net.komunan.komunantw.R
import net.komunan.komunantw.common.string
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.button
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.editText

internal class AuthUI: AnkoComponent<AuthFragment> {
    lateinit var openBrowser: Button
    lateinit var pin: EditText
    lateinit var submit: Button

    override fun createView(ui: AnkoContext<AuthFragment>) = with(ui) {
        constraintLayout {
            button(R.string.open_browser) {
                openBrowser = this
            }.lparams {
                topToTop = ConstraintSet.PARENT_ID
                startToStart = ConstraintSet.PARENT_ID
                endToEnd = ConstraintSet.PARENT_ID
            }

            editText {
                pin = this
                hint = R.string.pin.string()
            }.lparams {
                topToTop = ConstraintSet.PARENT_ID
                startToStart = ConstraintSet.PARENT_ID
                endToEnd = ConstraintSet.PARENT_ID
                bottomToBottom = ConstraintSet.PARENT_ID
            }

            button(R.string.authentication) {
                submit = this
            }.lparams {
                startToStart = ConstraintSet.PARENT_ID
                endToEnd = ConstraintSet.PARENT_ID
                bottomToBottom = ConstraintSet.PARENT_ID
            }
        }
    }
}
