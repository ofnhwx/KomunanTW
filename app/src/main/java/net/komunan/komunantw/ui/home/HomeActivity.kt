package net.komunan.komunantw.ui.home

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.LibsBuilder
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.komunan.komunantw.R
import net.komunan.komunantw.TWContext
import net.komunan.komunantw.common.extension.string
import net.komunan.komunantw.common.extension.withStringRes
import net.komunan.komunantw.common.service.TwitterService
import net.komunan.komunantw.repository.entity.Account
import net.komunan.komunantw.ui.account.auth.AccountAuthActivity
import net.komunan.komunantw.ui.account.list.AccountListActivity
import net.komunan.komunantw.ui.common.base.TWBaseActivity
import net.komunan.komunantw.ui.source.list.SourceListActivity
import net.komunan.komunantw.ui.timeline.list.TimelineListActivity

class HomeActivity: TWBaseActivity() {
    companion object {
        @JvmStatic
        fun createIntent(): Intent = Intent.makeRestartActivityTask(ComponentName(TWContext, HomeActivity::class.java))
    }

    override val layout = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkFirstRun()
        setupDrawer()
    }

    override fun onBackPressed() {
        TwitterService.garbageCleaning()
        super.onBackPressed()
    }

    override fun content(): Fragment? {
        return HomeFragment.create()
    }

    private fun checkFirstRun() {
        runBlocking(Dispatchers.Main) {
            if (withContext(Dispatchers.Default) { Account.dao.count() } == 0) {
                startActivity(AccountAuthActivity.createIntent(true))
            }
        }
    }

    private fun setupDrawer() {
        DrawerBuilder().apply {
            withActivity(this@HomeActivity)
            withToolbar(toolbar)
            addDrawerItems(
                    DividerDrawerItem(),
                    SecondaryDrawerItem().withStringRes(R.string.fragment_account_list),
                    SecondaryDrawerItem().withStringRes(R.string.fragment_timeline_list),
                    SecondaryDrawerItem().withStringRes(R.string.fragment_source_list),
                    DividerDrawerItem(),
                    SecondaryDrawerItem().withStringRes(R.string.license)
            )
            withOnDrawerItemClickListener { _, _, drawerItem ->
                when (drawerItem.identifier.toInt()) {
                    R.string.fragment_account_list -> startActivity(AccountListActivity.createIntent())
                    R.string.fragment_timeline_list -> startActivity(TimelineListActivity.createIntent())
                    R.string.fragment_source_list -> startActivity(SourceListActivity.createIntent())
                    R.string.license -> showLicense()
                }
                //drawer.closeDrawer()
                return@withOnDrawerItemClickListener true
            }
        }.build()
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
