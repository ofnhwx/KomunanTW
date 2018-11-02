package net.komunan.komunantw.ui.main.accounts

import androidx.lifecycle.LiveData
import net.komunan.komunantw.repository.entity.Account
import net.komunan.komunantw.ui.common.TWBaseViewModel

class AccountsViewModel: TWBaseViewModel() {
    val accounts: LiveData<List<Account>>
        get() = Account.findAllAsync()
}
