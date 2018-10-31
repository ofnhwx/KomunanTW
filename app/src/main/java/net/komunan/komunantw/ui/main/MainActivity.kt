package net.komunan.komunantw.ui.main

import android.arch.lifecycle.ViewModelProviders
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.Toolbar
import android.view.View
import com.github.ajalt.timberkt.d
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import net.komunan.komunantw.Preference
import net.komunan.komunantw.R
import net.komunan.komunantw.ReleaseApplication
import net.komunan.komunantw.ui.common.TWBaseActivity
import net.komunan.komunantw.ui.common.TWBaseViewModel
import net.komunan.komunantw.event.Transition
import net.komunan.komunantw.repository.entity.Account
import net.komunan.komunantw.service.TwitterService
import net.komunan.komunantw.ui.auth.AuthActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.toolbar
import java.util.*

class MainActivity: TWBaseActivity() {
    companion object {
        @JvmStatic
        fun newIntent(): Intent = Intent.makeRestartActivityTask(ComponentName(ReleaseApplication.context, MainActivity::class.java))
    }

    private val viewModel by lazy { ViewModelProviders.of(this).get(MainViewModel::class.java) }
    private val ui = MainActivityUI()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkFirstRun()
        ui.run {
            setContentView(this@MainActivity)
            setSupportActionBar(toolbar)
            setupDrawer()
            if (savedInstanceState == null) {
                setContent(HomeFragment.create())
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

    @Suppress("unused")
    @Subscribe
    fun onTransition(transition: Transition) {
        d { "event: $transition" }
        when (transition.target) {
            // Main[Home, Accounts, Timelines, Sources]
            Transition.Target.HOME -> setContent(HomeFragment.create())
            Transition.Target.ACCOUNTS -> setContent(AccountsFragment.create())
            Transition.Target.TIMELINES -> setContent(TimelinesFragment.create())
            Transition.Target.SOURCES -> setContent(SourcesFragment.create())
            // Auth
            Transition.Target.AUTH -> startActivity(AuthActivity.newIntent(false))
        }
    }

    private fun setupDrawer() {
        drawer = DrawerBuilder().apply {
            withActivity(this@MainActivity)
            withToolbar(ui.toolbar)
            addDrawerItems(
                    PrimaryDrawerItem().withIdentifier(R.string.home.toLong()).withName(R.string.home),
                    DividerDrawerItem(),
                    SecondaryDrawerItem().withIdentifier(R.string.account_list.toLong()).withName(R.string.account_list),
                    SecondaryDrawerItem().withIdentifier(R.string.timeline_list.toLong()).withName(R.string.timeline_list),
                    SecondaryDrawerItem().withIdentifier(R.string.source_list.toLong()).withName(R.string.source_list),
                    DividerDrawerItem()
            )
            withOnDrawerItemClickListener { _, _, drawerItem ->
                when (drawerItem.identifier) {
                    R.string.home.toLong() -> Transition.execute(Transition.Target.HOME)
                    R.string.account_list.toLong() -> Transition.execute(Transition.Target.ACCOUNTS)
                    R.string.timeline_list.toLong() -> Transition.execute(Transition.Target.TIMELINES)
                    R.string.source_list.toLong() -> Transition.execute(Transition.Target.SOURCES)
                }
                drawer.closeDrawer()
                return@withOnDrawerItemClickListener true
            }
        }.build()
    }

    private fun checkFirstRun() {
        launch(CommonPool) {
            if (Account.count() == 0) {
                startActivity(AuthActivity.newIntent(true))
            }
        }
    }

    private fun setContent(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(ui.container.id, fragment)
        }.commit()
    }
}

class MainViewModel: TWBaseViewModel() {
    private var timer = Timer()
    fun startUpdate() {
        timer.schedule(object: TimerTask() {
            override fun run() {
                TwitterService.fetchTweets()
            }
        }, 0, Preference.fetchIntervalMillis)
    }

    override fun onCleared() {
        super.onCleared()
        timer.cancel()
    }
}

private class MainActivityUI: AnkoComponent<MainActivity> {
    lateinit var toolbar: Toolbar
    lateinit var container: View

    override fun createView(ui: AnkoContext<MainActivity>) = with(ui) {
        verticalLayout {
            toolbar = toolbar {
                id = R.id.toolbar
            }.lparams(matchParent, wrapContent)
            container = frameLayout {
                id = R.id.container
            }.lparams(matchParent, dip(0), 1.0f)
        }
    }
}
