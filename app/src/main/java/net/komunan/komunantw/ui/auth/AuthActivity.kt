package net.komunan.komunantw.ui.auth

import android.arch.lifecycle.ViewModelProviders
import android.content.ComponentName
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.komunan.komunantw.R
import net.komunan.komunantw.ReleaseApplication
import net.komunan.komunantw.databinding.ActivityAuthBinding
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

    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_auth)
        binding.run {
            viewModel = ViewModelProviders.of(this@AuthActivity).get(AuthViewModel::class.java)
            openBrowser.setOnClickListener {
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
}
