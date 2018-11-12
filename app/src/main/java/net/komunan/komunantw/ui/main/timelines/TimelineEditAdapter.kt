package net.komunan.komunantw.ui.main.timelines

import android.net.Uri
import android.view.LayoutInflater
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
import net.komunan.komunantw.common.TWListAdapter
import net.komunan.komunantw.extension.make
import net.komunan.komunantw.extension.string
import net.komunan.komunantw.repository.entity.Account
import net.komunan.komunantw.repository.entity.Source
import net.komunan.komunantw.repository.entity.Timeline
import net.komunan.komunantw.repository.entity.ext.SourceWithActive

class TimelineEditAdapter(private val timelineId: Long): TWListAdapter<SourceWithActive, TimelineEditAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(timelineId, inflater.inflate(R.layout.item_source, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val timelineId: Long, itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(source: SourceWithActive) {
            GlobalScope.launch(Dispatchers.Main) {
                val account = withContext(Dispatchers.Default) { Account.dao.find(source.accountId)!! }
                itemView.source_account_icon.setImageURI(Uri.parse(account.imageUrl))
                itemView.source_account_name.text = account.name
                itemView.source_name.text = when (Source.Type.valueOf(source.type)) {
                    Source.Type.HOME -> string[R.string.home]()
                    Source.Type.MENTION -> string[R.string.mention]()
                    Source.Type.USER -> string[R.string.user]()
                    Source.Type.LIKE -> string[R.string.favorite]()
                    Source.Type.LIST -> string[R.string.format_list_label](source.label)
                    Source.Type.SEARCH -> string[R.string.format_search_label](source.label)
                }
                itemView.source_selected.run {
                    visibility = View.VISIBLE
                    setImageDrawable(GoogleMaterial.Icon.gmd_check.make(context).apply {
                        color(if (source.isActive) AppColor.GREEN else AppColor.GRAY)
                    })
                    setOnClickListener {
                        GlobalScope.launch {
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
}
