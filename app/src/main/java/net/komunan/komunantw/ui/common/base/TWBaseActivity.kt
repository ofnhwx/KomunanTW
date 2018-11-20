package net.komunan.komunantw.ui.common.base

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import net.komunan.komunantw.R

abstract class TWBaseActivity: AppCompatActivity() {
    @LayoutRes
    protected open val layout: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout ?: R.layout.activity_base)
        findViewById<Toolbar>(R.id.toolbar)?.let { toolbar ->
            setSupportActionBar(toolbar)
        }
        if (savedInstanceState == null) {
            content()?.let { fragment ->
                supportFragmentManager.beginTransaction()
                        .replace(R.id.content, fragment)
                        .commit()
            }
        }
    }

    protected open fun content(): Fragment? {
        return null
    }
}
