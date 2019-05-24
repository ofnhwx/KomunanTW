package net.komunan.komunantw.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import net.komunan.komunantw.common.Preference
import net.komunan.komunantw.ui.common.base.TWBaseViewModel

class HomeActivityViewModel : TWBaseViewModel() {
    private val page = MutableLiveData<Int>()

    val currentPage: LiveData<Int>
        get() = page

    fun setPage(page: Int) {
        if (Preference.currentPage != page) {
            Preference.currentPage = page
            this.page.postValue(page)
        }
    }
}
