package net.komunan.komunantw.ui.main.timelines.edit

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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
import net.komunan.komunantw.repository.entity.Timeline
import net.komunan.komunantw.repository.entity.ext.SourceWithActive
import net.komunan.komunantw.ui.common.base.TWListAdapter

class TimelineEditAdapter(private val timelineId: Long): TWListAdapter<SourceWithActive, TimelineEditAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(timelineId, inflate(R.layout.item_source, parent))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val timelineId: Long, itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(source: SourceWithActive) = GlobalScope.launch(Dispatchers.Main) {
            val account = withContext(Dispatchers.Default) { Account.dao.find(source.accountId)!! }
            itemView.source_account_icon.setImageURI(account.imageUrl)
            itemView.source_account_name.text = account.name
            itemView.source_name.text = source.displayName
            itemView.source_selected.apply {
                visibility = View.VISIBLE
                setImageDrawable(GoogleMaterial.Icon.gmd_check.make(context).apply {
                    color(if (source.isActive) AppColor.GREEN else AppColor.GRAY)
                })
                setOnClickListener {
                    GlobalScope.launch(Dispatchers.Default) {
                        if (source.isActive) {
                            Timeline.dao.find(timelineId)?.delSource(source)
                        } else {
                            Timeline.dao.find(timelineId)?.addSource(source)
                        }
                    }
                }
            }
        }
    }
}
