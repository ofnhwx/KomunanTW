package net.komunan.komunantw.ui.main

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProviders
import com.github.ajalt.timberkt.d
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.LibsBuilder
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.komunan.komunantw.R
import net.komunan.komunantw.TWContext
import net.komunan.komunantw.common.TWBaseActivity
import net.komunan.komunantw.event.Transition
import net.komunan.komunantw.extension.string
import net.komunan.komunantw.extension.withStringRes
import net.komunan.komunantw.repository.entity.Account
import net.komunan.komunantw.service.TwitterService
import net.komunan.komunantw.ui.auth.AuthActivity
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
        fun newIntent(): Intent = Intent.makeRestartActivityTask(ComponentName(TWContext, MainActivity::class.java))
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

    override fun onBackPressed() {
        when {
            supportFragmentManager.backStackEntryCount > 0 -> {
                super.onBackPressed()
            }
            supportFragmentManager.findFragmentById(container.id) is HomeFragment -> {
                TwitterService.garbageCleaning()
                super.onBackPressed()
            }
            else -> {
                setContent(HomeFragment.create())
                drawer.setSelection(R.string.home.toLong())
            }
        }
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
            // Others
            Transition.Target.AUTH -> startActivity(AuthActivity.newIntent(false))
            Transition.Target.LICENSE -> showLicense()
            // Back
            Transition.Target.BACK -> onBackPressed()
        }
    }

    private fun setupDrawer() {
        drawer = DrawerBuilder().apply {
            withActivity(this@MainActivity)
            withToolbar(toolbar)
            addDrawerItems(
                    PrimaryDrawerItem().withStringRes(R.string.home),
                    DividerDrawerItem(),
                    SecondaryDrawerItem().withStringRes(R.string.account_list),
                    SecondaryDrawerItem().withStringRes(R.string.timeline_list),
                    SecondaryDrawerItem().withStringRes(R.string.source_list),
                    DividerDrawerItem(),
                    SecondaryDrawerItem().withStringRes(R.string.license)
            )
            withOnDrawerItemClickListener { _, _, drawerItem ->
                when (drawerItem.identifier.toInt()) {
                    R.string.home -> Transition.execute(Transition.Target.HOME)
                    R.string.account_list -> Transition.execute(Transition.Target.ACCOUNTS)
                    R.string.timeline_list -> Transition.execute(Transition.Target.TIMELINES)
                    R.string.source_list -> Transition.execute(Transition.Target.SOURCES)
                    R.string.license -> Transition.execute(Transition.Target.LICENSE)
                }
                drawer.closeDrawer()
                return@withOnDrawerItemClickListener true
            }
        }.build()
    }

    private fun checkFirstRun() {
        GlobalScope.launch {
            if (Account.dao.count() == 0) {
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

    @Suppress("SpellCheckingInspection")
    private fun showLicense() {
        LibsBuilder()
                .withLibraries(
                        "CommonsLang",
                        "Fresco",
                        "FrescoImageViewer",
                        "PreferenceHolder",
                        "timberkt",
                        "Twitter4J",
                        "TwitterText"
                )
                .withFields(R.string::class.java.fields)
                .withActivityTitle(string[R.string.license]())
                .withActivityStyle(Libs.ActivityStyle.DARK)
                .start(this)
    }
}
