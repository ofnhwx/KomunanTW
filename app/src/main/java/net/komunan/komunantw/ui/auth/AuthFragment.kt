package net.komunan.komunantw.ui.auth

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.constraint.ConstraintSet.PARENT_ID
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import net.komunan.komunantw.R
import net.komunan.komunantw.common.string
import net.komunan.komunantw.repository.entity.ConsumerKeySecret
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.button
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.editText

class AuthFragment: Fragment() {
    companion object {
        @JvmStatic
        fun create() = AuthFragment()
    }

    private val viewModel by lazy { ViewModelProviders.of(this).get(AuthViewModel::class.java) }
    private val ui = AuthUI()
    private lateinit var consumerKeySecret: ConsumerKeySecret

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ui.createView(AnkoContext.create(context!!, this))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.consumerKeys.observe(this, Observer { consumerKeys ->
            consumerKeys?.let {
                consumerKeySecret = consumerKeys.first { consumerKeySecret -> consumerKeySecret.default }
            }
        })
        ui.openBrowser.setOnClickListener { v -> launch(UI) {
            v.isEnabled = false
            viewModel.startOAuth(consumerKeySecret).await()
            v.isEnabled = true
        }}
        ui.submit.setOnClickListener { v -> launch(UI) {
            v.isEnabled = false
            viewModel.finishOAuth(ui.pin.text.toString()).await()
            v.isEnabled = true
        }}
    }

    private class AuthUI: AnkoComponent<AuthFragment> {
        lateinit var openBrowser: Button
        lateinit var pin: EditText
        lateinit var submit: Button

        override fun createView(ui: AnkoContext<AuthFragment>) = with(ui) {
            constraintLayout {
                button(R.string.open_browser) {
                    openBrowser = this
                }.lparams {
                    topToTop = PARENT_ID
                    startToStart = PARENT_ID
                    endToEnd = PARENT_ID
                }

                editText {
                    pin = this
                    hint = R.string.pin.string()
                }.lparams {
                    topToTop = PARENT_ID
                    startToStart = PARENT_ID
                    endToEnd = PARENT_ID
                    bottomToBottom = PARENT_ID
                }

                button(R.string.authentication) {
                    submit = this
                }.lparams {
                    startToStart = PARENT_ID
                    endToEnd = PARENT_ID
                    bottomToBottom = PARENT_ID
                }
            }
        }
    }
}
