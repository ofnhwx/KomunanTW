package net.komunan.komunantw.ui.account.list

import androidx.lifecycle.LiveData
import net.komunan.komunantw.repository.entity.Account
import net.komunan.komunantw.ui.common.base.TWBaseViewModel

class AccountListViewModel: TWBaseViewModel() {
    val accounts: LiveData<List<Account>>
        get() = Account.dao.findAllAsync()
}
