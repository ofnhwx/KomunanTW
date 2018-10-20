package net.komunan.komunantw.ui.accounts

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import net.komunan.komunantw.repository.database.TWDatabase
import net.komunan.komunantw.repository.entity.AccountWithCredential

class AccountsViewModel(app: Application): AndroidViewModel(app) {
    val accounts: LiveData<List<AccountWithCredential>>
        get() = TWDatabase.instance.accountDao().findAll()
}
