package net.komunan.komunantw.ui.main.accounts

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_account.view.*
import net.komunan.komunantw.R
import net.komunan.komunantw.repository.entity.Account

class AccountsAdapter(private val accounts: List<Account>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_account, parent, false))
    }

    override fun getItemCount(): Int {
        return accounts.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(accounts[position])
    }

    private class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(account: Account) {
            itemView.account_icon.setImageURI(Uri.parse(account.imageUrl))
            itemView.account_name.text = account.name
            itemView.account_screen_name.text = account.screenName
        }
    }
}
