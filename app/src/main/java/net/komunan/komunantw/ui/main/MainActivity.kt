package net.komunan.komunantw.ui.main

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProviders
import com.github.ajalt.timberkt.d
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.komunan.komunantw.R
import net.komunan.komunantw.ReleaseApplication
import net.komunan.komunantw.event.Transition
import net.komunan.komunantw.repository.entity.Account
import net.komunan.komunantw.ui.auth.AuthActivity
import net.komunan.komunantw.common.TWBaseActivity
import net.komunan.komunantw.ui.main.accounts.AccountsFragment
import net.komunan.komunantw.ui.main.home.HomeFragment
import net.komunan.komunantw.ui.main.sources.SourcesFragment
import net.komunan.komunantw.ui.main.timelines.TimelineEditFragment
import net.komunan.komunantw.ui.main.timelines.TimelinesFragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class MainActivity: TWBaseActivity() {
    companion object {
        @JvmStatic
        fun newIntent(): Intent = Intent.makeRestartActivityTask(ComponentName(ReleaseApplication.context, MainActivity::class.java))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        checkFirstRun()
        setupDrawer()
        startUpdate()
        if (savedInstanceState == null) {
            setContent(HomeFragment.create())
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
            Transition.Target.TIMELINE_EDIT -> setContent(TimelineEditFragment.create(transition.id), true)
            Transition.Target.SOURCES -> setContent(SourcesFragment.create())
            // Auth
            Transition.Target.AUTH -> startActivity(AuthActivity.newIntent(false))
            // Back
            Transition.Target.BACK -> onBackPressed()
        }
    }

    private fun setupDrawer() {
        drawer = DrawerBuilder().apply {
            withActivity(this@MainActivity)
            withToolbar(toolbar)
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
        GlobalScope.launch {
            if (Account.count() == 0) {
                startActivity(AuthActivity.newIntent(true))
            }
        }
    }

    private fun startUpdate() {
        ViewModelProviders.of(this).get(MainViewModel::class.java).startUpdate()
    }

    private fun setContent(fragment: Fragment, child: Boolean = false) {
        supportFragmentManager.run {
            if (!child && backStackEntryCount > 0) {
                popBackStack(getBackStackEntryAt(0).id, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
            beginTransaction().apply {
                replace(container.id, fragment)
                if (child) { addToBackStack(null) }
            }.commit()
        }
    }
}
