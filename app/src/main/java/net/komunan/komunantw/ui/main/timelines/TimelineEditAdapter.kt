package net.komunan.komunantw.ui.main.timelines

import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.item_source.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.komunan.komunantw.R
import net.komunan.komunantw.common.TWListAdapter
import net.komunan.komunantw.repository.entity.Source
import net.komunan.komunantw.repository.entity.SourceForSelect
import net.komunan.komunantw.repository.entity.Timeline
import net.komunan.komunantw.string

class TimelineEditAdapter(private val timelineId: Long): TWListAdapter<SourceForSelect, TimelineEditAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(timelineId, inflater.inflate(R.layout.item_source, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(val timelineId: Long, itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(source: SourceForSelect) {
            GlobalScope.launch(Dispatchers.Main) {
                val account = withContext(Dispatchers.Default) { source.account() }
                if (account == null) {
                    // TODO: あとで考える
                } else {
                    itemView.source_account_icon.setImageURI(Uri.parse(account.imageUrl))
                    itemView.source_account_name.text = account.name
                }
                itemView.source_name.text = when (Source.SourceType.valueOf(source.type)) {
                    Source.SourceType.HOME -> R.string.home.string()
                    Source.SourceType.MENTION -> R.string.mention.string()
                    Source.SourceType.USER -> R.string.user.string()
                    Source.SourceType.LIKE -> R.string.favorite.string()
                    Source.SourceType.LIST -> R.string.format_list_label.string(source.label)
                    Source.SourceType.SEARCH -> R.string.format_search_label.string(source.label)
                }
                itemView.source_selected.run {
                    visibility = View.VISIBLE
                    if (source.isActive) {
                        setImageDrawable(IconicsDrawable(context).icon(GoogleMaterial.Icon.gmd_check).color(Color.GREEN))
                        setOnClickListener {
                            GlobalScope.launch { Timeline.find(timelineId)?.removeSource(source) }
                        }
                    } else {
                        setImageDrawable(IconicsDrawable(context).icon(GoogleMaterial.Icon.gmd_check).color(Color.GRAY))
                        setOnClickListener {
                            GlobalScope.launch { Timeline.find(timelineId)?.addSource(source) }
                        }
                    }
                }
            }
        }
    }
}
