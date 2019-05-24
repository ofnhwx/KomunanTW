package net.komunan.komunantw.core.repository

import android.content.Context
import io.objectbox.BoxStore
import net.komunan.komunantw.core.repository.entity.MyObjectBox

class ObjectBox {
    companion object {
        private lateinit var _boxStore: BoxStore

        @JvmStatic
        fun init(context: Context) {
            _boxStore = MyObjectBox.builder()
                    .androidContext(context)
                    .build()
        }

        @JvmStatic
        fun get(): BoxStore = _boxStore
    }
}
