package net.komunan.komunantw.ui.common

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.content.Context
import net.komunan.komunantw.ReleaseApplication

open class TWBaseViewModel: ViewModel() {
    private val _isProcessing = MutableLiveData<Boolean>()

    init {
        _isProcessing.postValue(false)
    }

    val isIdle: LiveData<Boolean>
        get() = Transformations.map(_isProcessing) { !it }

    val context: Context
        get() = ReleaseApplication.context

    val application: ReleaseApplication
        get() = ReleaseApplication.instance

    protected suspend fun <R> process(body: suspend () -> R): R {
        try {
            _isProcessing.postValue(true)
            return body()
        } finally {
            _isProcessing.postValue(false)
        }
    }
}
