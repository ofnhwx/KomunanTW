package net.komunan.komunantw.ui.auth

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import net.komunan.komunantw.common.observeOnNotNull
import net.komunan.komunantw.repository.entity.ConsumerKeySecret
import org.jetbrains.anko.AnkoContext

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
        viewModel.run {
            consumerKeys().observeOnNotNull(this@AuthFragment) { consumerKeys ->
                consumerKeySecret = consumerKeys.first { consumerKeySecret -> consumerKeySecret.default }
            }
            isProcessing.observeOnNotNull(this@AuthFragment) { isProcessing ->
                ui.openBrowser.isEnabled = !isProcessing
                ui.submit.isEnabled = !isProcessing
            }
        }
        ui.run {
            ui.openBrowser.setOnClickListener { launch(CommonPool) {
                viewModel.startOAuth(consumerKeySecret)
            }}
            ui.submit.setOnClickListener { launch(CommonPool) {
                viewModel.finishOAuth(ui.pin.text.toString())
            }}
        }
    }
}
