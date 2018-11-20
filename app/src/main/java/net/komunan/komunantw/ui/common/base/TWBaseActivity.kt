package net.komunan.komunantw.ui.common.base

import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import net.komunan.komunantw.R

abstract class TWBaseActivity: AppCompatActivity() {
    @LayoutRes
    protected open val layout: Int? = null
    protected open val content: Fragment? = null
    protected open val upNavigation: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout ?: R.layout.activity_base)
        findViewById<Toolbar>(R.id.toolbar)?.let { toolbar ->
            setSupportActionBar(toolbar)
        }
        if (upNavigation) {
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                setDisplayShowHomeEnabled(true)
            }
        }
        if (savedInstanceState == null) {
            content?.also { content ->
                supportFragmentManager.beginTransaction()
                        .replace(R.id.content, content)
                        .commit()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return if (item?.itemId == android.R.id.home) {
            upNavigation()
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    protected open fun upNavigation(): Boolean {
        finish()
        return true
    }

    protected fun <T: ViewModel> viewModel(clazz: Class<T>): T {
        return ViewModelProviders.of(this).get(clazz)
    }
}
