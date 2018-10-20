package net.komunan.komunantw

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.mikepenz.materialdrawer.Drawer
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import net.komunan.komunantw.event.Transition
import net.komunan.komunantw.repository.database.TWDatabase
import net.komunan.komunantw.ui.accounts.AccountsFragment
import net.komunan.komunantw.ui.auth.AuthFragment
import net.komunan.komunantw.ui.timeline.TimelineFragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.setContentView

class TWActivity: AppCompatActivity() {
    private lateinit var drawer: Drawer
    private lateinit var ui: TWActivityUI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = TWActivityUI().also {
            it.setContentView(this)
        }
        if (savedInstanceState == null) {
            setupDrawer()
            showStartup()
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

    @Suppress("unused")
    @Subscribe
    fun onTransition(transition: Transition) {
        when (transition.getTarget()) {
            Transition.Target.ACCOUNTS -> setContent(AccountsFragment.create())
            Transition.Target.AUTH -> setContent(AuthFragment.create())
            Transition.Target.TIMELINE -> setContent(TimelineFragment.create())
        }
    }

    private fun setupDrawer() {
    }

    private fun showStartup() = launch(UI) {
        val fragment: Deferred<Fragment> = async {
            if (TWDatabase.instance.accountDao().count() == 0) {
                return@async AuthFragment.create()
            } else {
                return@async TimelineFragment.create()
            }
        }
        setContent(fragment.await())
    }

    private fun setContent(fragment: Fragment) {
        supportFragmentManager.beginTransaction().also {
            it.replace(ui.container.id, fragment)
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
