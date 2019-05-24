package net.komunan.komunantw.ui.timeline.edit

import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import kotlinx.android.synthetic.main.item_source.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.komunan.komunantw.R
import net.komunan.komunantw.common.AppColor
import net.komunan.komunantw.common.make
import net.komunan.komunantw.core.repository.entity.Source
import net.komunan.komunantw.databinding.ItemSourceBinding
import net.komunan.komunantw.ui.common.base.TWBaseListAdapter

class TimelineEditAdapter : TWBaseListAdapter<Source, TimelineEditAdapter.ViewHolder>() {
    var onClickEvent: ((source: Source) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(inflate(R.layout.item_source, parent))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : TWBaseListAdapter.ViewHolder<Source>(itemView) {
        private val binding by lazy { DataBindingUtil.bind<ItemSourceBinding>(itemView)!! }

        override fun bind(item: Source) {
            GlobalScope.launch(Dispatchers.Main) {
                binding.account = withContext(Dispatchers.Default) { item.account.target }
            }
            binding.source = item
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
