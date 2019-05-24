package net.komunan.komunantw.ui.account.list

import androidx.lifecycle.LiveData
import io.objectbox.android.ObjectBoxLiveData
import net.komunan.komunantw.core.repository.entity.Account
import net.komunan.komunantw.core.repository.entity.Account_
import net.komunan.komunantw.ui.common.base.TWBaseViewModel

class AccountListViewModel : TWBaseViewModel() {
    val accounts: LiveData<List<Account>>
        get() = ObjectBoxLiveData(Account.query().apply {
            order(Account_.name)
        }.build())
}
