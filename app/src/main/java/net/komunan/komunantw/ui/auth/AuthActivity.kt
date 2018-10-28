package net.komunan.komunantw.ui.auth

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import net.komunan.komunantw.ReleaseApplication
import net.komunan.komunantw.common.BaseActivity
import net.komunan.komunantw.common.observeOnNotNull
import net.komunan.komunantw.repository.entity.ConsumerKeySecret
import net.komunan.komunantw.ui.activity.TWActivity
import org.jetbrains.anko.setContentView

@SuppressLint("Registered")
class AuthActivity: BaseActivity() {
    companion object {
        private const val PARAMETER_FIRST_RUN = "AuthActivity.PARAMETER_FIRST_RUN"

        @JvmStatic
        fun newIntent(firstRun: Boolean = false) = Intent(ReleaseApplication.context, AuthActivity::class.java).apply {
            putExtra(PARAMETER_FIRST_RUN, firstRun)
        }
    }

    private val viewModel by lazy { ViewModelProviders.of(this).get(AuthViewModel::class.java) }
    private val ui = AuthUI()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui.run {
            setContentView(this@AuthActivity)
            ui.openBrowser.setOnClickListener {
                launch(CommonPool) {
                    val consumerKeySecret = ConsumerKeySecret.findAll().first()
                    viewModel.startOAuth(consumerKeySecret)
                }
            }
            ui.submit.setOnClickListener {
                launch(CommonPool) {
                    viewModel.finishOAuth(ui.pin.text.toString())
                    if (intent.getBooleanExtra(PARAMETER_FIRST_RUN, false)) {
                        startActivity(TWActivity.newIntent())
                    } else {
                        finish()
                    }
                }
            }
        }
        viewModel.run {
            isProcessing.observeOnNotNull(this@AuthActivity) { isProcessing ->
                ui.openBrowser.isEnabled = !isProcessing
                ui.submit.isEnabled = !isProcessing
            }
        }
    }
}
