package net.komunan.komunantw.common

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import net.komunan.komunantw.ReleaseApplication

open class BaseViewModel: ViewModel() {
    private val _isProcessing: MutableLiveData<Boolean> = MutableLiveData()
    val isProcessing: LiveData<Boolean>
        get() = _isProcessing

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
