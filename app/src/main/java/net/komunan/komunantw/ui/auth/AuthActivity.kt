package net.komunan.komunantw.ui.auth

import android.annotation.SuppressLint
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModelProviders
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.github.ajalt.timberkt.w
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import net.komunan.komunantw.*
import net.komunan.komunantw.repository.database.transaction
import net.komunan.komunantw.repository.entity.*
import net.komunan.komunantw.service.TwitterService
import net.komunan.komunantw.ui.common.TWBaseActivity
import net.komunan.komunantw.ui.common.TWBaseViewModel
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
    private val disposables = CompositeDisposable()

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
                launch(UI) {
                    if (!withContext(CommonPool) { viewModel.finishOAuth() }) {
                        Toast.makeText(this@AuthActivity, "認証に失敗しました", Toast.LENGTH_SHORT).show() // TODO: ちゃんとメッセージを考える
                        return@launch
                    }
                    if (intent.getBooleanExtra(PARAMETER_FIRST_RUN, false)) {
                        startActivity(MainActivity.newIntent())
                    } else {
                        finish()
                    }
                }
            }
            disposables.add(ui.pin.textChanges().subscribe {
                viewModel.pin.postValue(it.toString())
            })
        }
        viewModel.run {
            isPinEnabled.observeOnNotNull(this@AuthActivity) { isPinEnabled ->
                ui.pin.isEnabled = isPinEnabled
            }
            isOpenBrowserEnabled.observeOnNotNull(this@AuthActivity) { isOpenBrowserEnabled ->
                ui.openBrowser.isEnabled = isOpenBrowserEnabled
            }
            isSubmitEnabled.observeOnNotNull(this@AuthActivity) { isSubmitEnabled ->
                ui.submit.isEnabled = isSubmitEnabled
            }
        }
    }

    override fun onDestroy() {
        disposables.dispose()
        super.onDestroy()
    }
}

class AuthViewModel: TWBaseViewModel() {
    private val isTokenValid = MutableLiveData<Boolean>()

    val pin = MutableLiveData<String>()
    val isPinEnabled = isTokenValid as LiveData<Boolean>
    val isOpenBrowserEnabled = isIdle
    val isSubmitEnabled = combineLatest(isIdle, pin) { isIdle, pin ->
        return@combineLatest (isIdle ?: false) && (pin?.length == 7)
    }

    init {
        isTokenValid.postValue(Preference.requestToken != null && Preference.consumerKeySecret != null)
        pin.postValue("")
    }

    suspend fun startOAuth(consumerKeySecret: ConsumerKeySecret) = process {
        val requestToken = TwitterService.twitter(consumerKeySecret).oAuthRequestToken
        Preference.requestToken = requestToken
        Preference.consumerKeySecret = consumerKeySecret
        isTokenValid.postValue(true)
        application.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.authorizationURL)))
    }

    suspend fun finishOAuth() = process {
        val consumerKeySecret = Preference.consumerKeySecret
        val requestToken = Preference.requestToken
        if (consumerKeySecret == null || requestToken == null) {
            w { "ConsumerKeySecret or RequestToken is null: ConsumerKeySecret=$consumerKeySecret, RequestToken=$requestToken" }
            return@process false
        }

        val twitter = TwitterService.twitter(consumerKeySecret)
        try {
            val accessToken = twitter.getOAuthAccessToken(requestToken, pin.value)
            transaction {
                val account = Account(twitter.showUser(accessToken.userId)).save()
                Credential.findByAccountId(account.id).forEach { it.delete() }
                Credential(account, consumerKeySecret, accessToken).save()
                Source.update(account)
                Timeline.firstSetup(account)
                // 暫定対応
                Timeline.find(1)?.addSource(Source.findByAccountId(account.id).first())
            }
            Preference.consumerKeySecret = null
            Preference.requestToken = null
            isTokenValid.postValue(false)
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
