package net.komunan.komunantw.common

import android.support.v7.app.AppCompatActivity
import com.mikepenz.materialdrawer.Drawer

abstract class BaseActivity: AppCompatActivity() {
    protected lateinit var drawer: Drawer
}
