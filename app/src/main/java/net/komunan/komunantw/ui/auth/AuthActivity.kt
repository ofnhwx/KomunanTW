package net.komunan.komunantw.ui.auth

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.github.ajalt.timberkt.w
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import net.komunan.komunantw.Preference
import net.komunan.komunantw.R
import net.komunan.komunantw.ReleaseApplication
import net.komunan.komunantw.ui.common.TWBaseActivity
import net.komunan.komunantw.ui.common.TWBaseViewModel
import net.komunan.komunantw.observeOnNotNull
import net.komunan.komunantw.string
import net.komunan.komunantw.repository.database.transaction
import net.komunan.komunantw.repository.entity.*
import net.komunan.komunantw.service.TwitterService
import net.komunan.komunantw.ui.main.MainActivity
import org.jetbrains.anko.*
import twitter4j.TwitterException

@SuppressLint("Registered")
class AuthActivity: TWBaseActivity() {
    companion object {
        private const val PARAMETER_FIRST_RUN = "AuthActivity.PARAMETER_FIRST_RUN"

        @JvmStatic
        fun newIntent(firstRun: Boolean = false): Intent {
            val componentName = ComponentName(ReleaseApplication.context, AuthActivity::class.java)
            val intent = if (firstRun) Intent.makeRestartActivityTask(componentName) else Intent.makeMainActivity(componentName)
            return intent.apply {
                putExtra(PARAMETER_FIRST_RUN, firstRun)
            }
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
                        startActivity(MainActivity.newIntent())
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

    private class AuthViewModel: TWBaseViewModel() {
        //fun consumerKeys() = ConsumerKeySecret.findAllAsync()

        suspend fun startOAuth(consumerKeySecret: ConsumerKeySecret) = process {
            val requestToken = TwitterService.twitter(consumerKeySecret).oAuthRequestToken
            Preference.requestToken = requestToken
            Preference.consumerKeySecret = consumerKeySecret
            application.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.authorizationURL)))
        }

        suspend fun finishOAuth(pin: String) = process {
            val consumerKeySecret = Preference.consumerKeySecret
            val requestToken = Preference.requestToken
            if (consumerKeySecret == null || requestToken == null) {
                w { "ConsumerKeySecret or RequestToken is null: ConsumerKeySecret=$consumerKeySecret, RequestToken=$requestToken" }
                return@process false
            }

            val twitter = TwitterService.twitter(consumerKeySecret)
            try {
                val accessToken = twitter.getOAuthAccessToken(requestToken, pin)
                transaction {
                    val account = Account(twitter.showUser(accessToken.userId)).save()
                    Credential(account, consumerKeySecret, accessToken).save()
                    Source.update(account)
                    Timeline.firstSetup(account)
                    // 暫定対応
                    Timeline.find(1).addSource(Source.findByAccountId(account.id).first())
                }
                Preference.consumerKeySecret = null
                Preference.requestToken = null
                return@process true
            } catch (e: TwitterException) {
                w(e)
                return@process false
            }
        }
    }

    private class AuthUI: AnkoComponent<AuthActivity> {
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
}
