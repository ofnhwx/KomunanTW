package net.komunan.komunantw.ui.accounts

import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import net.komunan.komunantw.R
import net.komunan.komunantw.common.draweeView
import net.komunan.komunantw.common.string
import net.komunan.komunantw.repository.entity.Account
import org.jetbrains.anko.*


internal class AccountUI: AnkoComponent<ViewGroup> {
    lateinit var userIcon: ImageView
    lateinit var userName: TextView
    lateinit var screenName: TextView

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        linearLayout {
            userIcon = draweeView {
                id = R.id.account_icon
            }.lparams(dip(48), dip(48))
            verticalLayout {
                userName = textView {
                    id = R.id.account_name
                }
                screenName = textView {
                    id = R.id.account_screen_name
                }
            }
        }
    }

    fun bind(account: Account) {
        userIcon.setImageURI(Uri.parse(account.imageUrl))
        userName.text = account.name
        screenName.text = R.string.format_screen_name.string(account.screenName)
    }
}
