package net.komunan.komunantw.ui.timeline.edit

import android.view.View
import android.view.ViewGroup
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import kotlinx.android.synthetic.main.item_source.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.komunan.komunantw.R
import net.komunan.komunantw.common.AppColor
import net.komunan.komunantw.common.extension.make
import net.komunan.komunantw.repository.entity.Account
import net.komunan.komunantw.repository.entity.ext.SourceWithActive
import net.komunan.komunantw.ui.common.base.TWBaseListAdapter

class TimelineEditAdapter: TWBaseListAdapter<SourceWithActive, TimelineEditAdapter.ViewHolder>() {
    var onClickEvent: ((source: SourceWithActive) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(inflate(R.layout.item_source, parent))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View): TWBaseListAdapter.ViewHolder<SourceWithActive>(itemView) {
        override fun bind(item: SourceWithActive) {
            GlobalScope.launch(Dispatchers.Main) {
                val account = withContext(Dispatchers.Default) { Account.dao.find(item.accountId)!! }
                itemView.source_account_icon.setImageURI(account.imageUrl)
                itemView.source_account_name.text = account.name
                itemView.source_name.text = item.displayName
                itemView.source_selected.apply {
                    visibility = View.VISIBLE
                    setImageDrawable(GoogleMaterial.Icon.gmd_check.make(context).apply {
                        color(if (item.isActive) AppColor.GREEN else AppColor.GRAY)
                    })
                    setOnClickListener { onClickEvent?.invoke(item) }
                }
            }
        }
    }
}
