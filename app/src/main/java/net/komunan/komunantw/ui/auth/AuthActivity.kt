package net.komunan.komunantw.ui.auth

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_auth.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.komunan.komunantw.R
import net.komunan.komunantw.ReleaseApplication
import net.komunan.komunantw.observeOnNotNull
import net.komunan.komunantw.repository.entity.ConsumerKeySecret
import net.komunan.komunantw.ui.common.TWBaseActivity
import net.komunan.komunantw.ui.main.MainActivity

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        val viewModel = ViewModelProviders.of(this).get(AuthViewModel::class.java)
        viewModel.pinEnabled.observeOnNotNull(this) {  pin.isEnabled = it }
        viewModel.openBrowserEnabled.observeOnNotNull(this) { open_browser.isEnabled = it }
        viewModel.authenticationEnabled.observeOnNotNull(this) { authentication.isEnabled = it }

        pin.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                viewModel.pin.postValue(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        open_browser.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                viewModel.startOAuth(ConsumerKeySecret.default())
            }
        }
        authentication.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                if (withContext(Dispatchers.Default) { !viewModel.finishOAuth() }) {
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
    }
}
