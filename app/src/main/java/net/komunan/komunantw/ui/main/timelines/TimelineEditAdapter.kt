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
import net.komunan.komunantw.repository.entity.Source
import net.komunan.komunantw.repository.entity.SourceForSelect
import net.komunan.komunantw.repository.entity.Timeline

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
                    Source.SourceType.HOME -> string[R.string.home]()
                    Source.SourceType.MENTION -> string[R.string.mention]()
                    Source.SourceType.USER -> string[R.string.user]()
                    Source.SourceType.LIKE -> string[R.string.favorite]()
                    Source.SourceType.LIST -> string[R.string.format_list_label](source.label)
                    Source.SourceType.SEARCH -> string[R.string.format_search_label](source.label)
                }
                itemView.source_selected.run {
                    visibility = View.VISIBLE
                    if (source.isActive) {
                        setImageDrawable(GoogleMaterial.Icon.gmd_check.make(context).color(AppColor.GREEN))
                        setOnClickListener {
                            GlobalScope.launch { Timeline.find(timelineId)?.removeSource(source) }
                        }
                    } else {
                        setImageDrawable(GoogleMaterial.Icon.gmd_check.make(context).color(AppColor.GRAY))
                        setOnClickListener {
                            GlobalScope.launch { Timeline.find(timelineId)?.addSource(source) }
                        }
                    }
                }
            }
        }
    }
}
