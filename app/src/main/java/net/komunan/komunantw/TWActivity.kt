package net.komunan.komunantw

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.mikepenz.materialdrawer.Drawer
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import net.komunan.komunantw.event.Transition
import net.komunan.komunantw.repository.entity.Account
import net.komunan.komunantw.ui.accounts.AccountsFragment
import net.komunan.komunantw.ui.auth.AuthFragment
import net.komunan.komunantw.ui.timelines.TimelinesFragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.setContentView

class TWActivity: AppCompatActivity() {
    private lateinit var drawer: Drawer
    private val viewModel by lazy { ViewModelProviders.of(this).get(TWViewModel::class.java) }
    private val ui = TWActivityUI()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui.run {
            setContentView(this@TWActivity)
            if (savedInstanceState == null) {
                setupDrawer()
                showStartup()
            }
        }
        viewModel.run {
            startUpdate()
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onBackPressed() {
        supportFragmentManager.run {
            when {
                backStackEntryCount > 0 -> popBackStack()
                findFragmentById(R.id.container) is TimelinesFragment -> super.onBackPressed()
                else -> showStartup()
            }
        }
    }

    @Suppress("unused")
    @Subscribe
    fun onTransition(transition: Transition) {
        when (transition.target) {
            Transition.Target.ACCOUNTS -> setContent(AccountsFragment.create(), transition.isChild)
            Transition.Target.AUTH -> setContent(AuthFragment.create(), transition.isChild)
            Transition.Target.TIMELINE -> setContent(TimelinesFragment.create(), transition.isChild)
            Transition.Target.BACK -> onBackPressed()
        }
    }

    private fun setupDrawer() {
    }

    private fun showStartup() = launch(UI) {
        val count = async(CommonPool) { Account.count() }
        val fragment = if (count.await() == 0) {
            AuthFragment.create()
        } else {
            TimelinesFragment.create()
        }
        setContent(fragment, false)
    }

    private fun setContent(fragment: Fragment, isChild: Boolean) {
        supportFragmentManager.beginTransaction().apply {
            replace(ui.container.id, fragment)
            if (isChild) {
                addToBackStack(null)
            }
        }.commit()
    }

    private class TWActivityUI: AnkoComponent<TWActivity> {
        lateinit var container: View

        override fun createView(ui: AnkoContext<TWActivity>) = with(ui) {
            frameLayout {
                id = R.id.container
            }.also {
                container = it
            }
        }
    }
}
