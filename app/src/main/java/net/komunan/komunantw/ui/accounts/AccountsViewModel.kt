package net.komunan.komunantw.ui.accounts

import net.komunan.komunantw.common.BaseViewModel
import net.komunan.komunantw.repository.entity.Account

class AccountsViewModel: BaseViewModel() {
    fun accounts() = Account.findAllAsync()
}
