package net.komunan.komunantw.ui.account.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_account_auth.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.komunan.komunantw.R
import net.komunan.komunantw.common.string
import net.komunan.komunantw.core.repository.entity.Consumer
import net.komunan.komunantw.ui.common.base.TWBaseFragment
import net.komunan.komunantw.ui.home.HomeActivity

class AccountAuthFragment : TWBaseFragment() {
    companion object {
        private const val PARAMETER_FIRST_RUN = "AccountAuthFragment.PARAMETER_FIRST_RUN"

        @JvmStatic
        fun create(firstRun: Boolean = false): AccountAuthFragment {
            return AccountAuthFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(PARAMETER_FIRST_RUN, firstRun)
                }
            }
        }
    }

    override val name: String?
        get() = string[R.string.fragment_account_auth]()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_account_auth, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val viewModel = viewModel(AccountAuthViewModel::class.java)
        viewModel.pinEnabled.observe(this, Observer { pin.isEnabled = it })
        viewModel.openBrowserEnabled.observe(this, Observer { open_browser.isEnabled = it })
        viewModel.authenticationEnabled.observe(this, Observer { authentication.isEnabled = it })

        pin.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.pin.postValue(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        open_browser.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                withContext(Dispatchers.Default) { viewModel.startOAuth(Consumer.default) }
            }
        }

        authentication.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                when {
                    withContext(Dispatchers.Default) { !viewModel.finishOAuth() } -> {
                        Toast.makeText(context!!, R.string.message_authentication_failed, Toast.LENGTH_SHORT).show()
                    }
                    arguments?.getBoolean(PARAMETER_FIRST_RUN) == true -> {
                        startActivity(HomeActivity.createIntent())
                    }
                    else -> {
                        activity?.finish()
                    }
                }
            }
        }
    }
}
