package net.komunan.komunantw.ui.accounts

import android.support.constraint.ConstraintSet
import android.support.design.widget.FloatingActionButton
import android.widget.ListView
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.design.floatingActionButton
import org.jetbrains.anko.dip
import org.jetbrains.anko.listView

internal class AccountsUI: AnkoComponent<AccountsFragment> {
    lateinit var accounts: ListView
    lateinit var add: FloatingActionButton

    override fun createView(ui: AnkoContext<AccountsFragment>) = with(ui) {
        constraintLayout {
            listView {
                accounts = this
            }.lparams(0, 0) {
                topToTop = ConstraintSet.PARENT_ID
                startToStart = ConstraintSet.PARENT_ID
                endToEnd = ConstraintSet.PARENT_ID
                bottomToBottom = ConstraintSet.PARENT_ID
            }

            floatingActionButton {
                add = this
            }.lparams {
                endToEnd = ConstraintSet.PARENT_ID
                bottomToBottom = ConstraintSet.PARENT_ID
                rightMargin = dip(16.0f)
                bottomMargin = dip(16.0f)
            }
        }
    }
}
