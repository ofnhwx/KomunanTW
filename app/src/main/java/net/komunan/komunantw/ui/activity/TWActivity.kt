package net.komunan.komunantw.ui.activity

import android.arch.lifecycle.ViewModelProviders
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import com.crashlytics.android.Crashlytics
import com.github.ajalt.timberkt.d
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import io.fabric.sdk.android.Fabric
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import net.komunan.komunantw.R
import net.komunan.komunantw.ReleaseApplication
import net.komunan.komunantw.common.BaseActivity
import net.komunan.komunantw.event.Transition
import net.komunan.komunantw.repository.entity.Account
import net.komunan.komunantw.ui.accounts.AccountsFragment
import net.komunan.komunantw.ui.auth.AuthActivity
import net.komunan.komunantw.ui.home.HomeFragment
import net.komunan.komunantw.ui.sources.SourcesFragment
import net.komunan.komunantw.ui.timelines.TimelinesFragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.setContentView

class TWActivity: BaseActivity() {
    companion object {
        @JvmStatic
        fun newIntent() = Intent.makeRestartActivityTask(ComponentName(ReleaseApplication.context, TWActivity::class.java))
    }

    private val viewModel by lazy { ViewModelProviders.of(this).get(TWViewModel::class.java) }
    private val ui = TWActivityUI()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        checkFirstRun()
        ui.run {
            setContentView(this@TWActivity)
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
            Transition.Target.HOME -> setContent(HomeFragment.create())
            Transition.Target.ACCOUNTS -> setContent(AccountsFragment.create())
            Transition.Target.AUTH -> startActivity(AuthActivity.newIntent())
            Transition.Target.TIMELINES -> setContent(TimelinesFragment.create())
            Transition.Target.SOURCES -> setContent(SourcesFragment.create())
        }
    }

    private fun setupDrawer() {
        drawer = DrawerBuilder().apply {
            withActivity(this@TWActivity)
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

    private fun checkFirstRun() = launch(CommonPool) {
        if (Account.count() == 0) {
            startActivity(AuthActivity.newIntent(true))
            finish()
        }
    }

    private fun setContent(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(ui.container.id, fragment)
        }.commit()
    }
}
