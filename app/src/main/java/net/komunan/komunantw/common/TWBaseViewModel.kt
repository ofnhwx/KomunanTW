package net.komunan.komunantw.common

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
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

    protected suspend fun <R> process(body: suspend () -> R): R {
        try {
            _isProcessing.postValue(true)
            return body()
        } finally {
            _isProcessing.postValue(false)
        }
    }
}
