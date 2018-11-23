package net.komunan.komunantw.ui.home

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.LibsBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import net.komunan.komunantw.R
import net.komunan.komunantw.TWContext
import net.komunan.komunantw.common.Preference
import net.komunan.komunantw.common.extension.observeOnNotNull
import net.komunan.komunantw.common.extension.string
import net.komunan.komunantw.common.extension.withStringRes
import net.komunan.komunantw.common.service.TwitterService
import net.komunan.komunantw.repository.entity.Account
import net.komunan.komunantw.repository.entity.Timeline
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

    private lateinit var drawer: Drawer

    override val layout = R.layout.activity_main
    override val upNavigation = false
    override val content: Fragment?
        get() = HomeFragment.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkFirstRun()
        setupDrawer()
    }

    override fun onBackPressed() {
        TwitterService.garbageCleaning()
        super.onBackPressed()
    }

    private fun checkFirstRun() {
        GlobalScope.launch(Dispatchers.Main) {
            if (withContext(Dispatchers.Default) { Account.dao.count() } == 0) {
                startActivity(AccountAuthActivity.createIntent(true))
            }
        }
    }

    private fun setupDrawer() = GlobalScope.launch(Dispatchers.Main) {
        val viewModel = viewModel(HomeActivityViewModel::class.java)
        drawer = DrawerBuilder().apply {
            withActivity(this@HomeActivity)
            withToolbar(toolbar)
            withContext(Dispatchers.Default) { Timeline.dao.findAll() }.forEach { timeline ->
                addDrawerItems(SecondaryDrawerItem().withIdentifier(timeline.position.toLong()).withName(timeline.name))
            }
            addDrawerItems(
                    DividerDrawerItem(),
                    SecondaryDrawerItem().withStringRes(R.string.fragment_account_list).withSelectable(false),
                    SecondaryDrawerItem().withStringRes(R.string.fragment_timeline_list).withSelectable(false),
                    SecondaryDrawerItem().withStringRes(R.string.fragment_source_list).withSelectable(false),
                    DividerDrawerItem(),
                    SecondaryDrawerItem().withStringRes(R.string.license).withSelectable(false)
            )
            withOnDrawerItemClickListener { _, _, drawerItem ->
                when (drawerItem.identifier.toInt()) {
                    R.string.fragment_account_list -> startActivity(AccountListActivity.createIntent())
                    R.string.fragment_timeline_list -> startActivity(TimelineListActivity.createIntent())
                    R.string.fragment_source_list -> startActivity(SourceListActivity.createIntent())
                    R.string.license -> showLicense()
                    else -> viewModel.setPage(drawerItem.identifier.toInt())
                }
                drawer.closeDrawer()
                return@withOnDrawerItemClickListener true
            }
        }.build()
        drawer.setSelection(Preference.currentPage.toLong())
        viewModel.currentPage.observeOnNotNull(this@HomeActivity) { currentPage ->
            drawer.setSelection(currentPage.toLong())
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
