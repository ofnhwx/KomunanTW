package net.komunan.komunantw.ui.common.base

import androidx.appcompat.app.AppCompatActivity
import com.mikepenz.materialdrawer.Drawer

abstract class TWBaseActivity: AppCompatActivity() {
    protected lateinit var drawer: Drawer
}
