package net.komunan.komunantw.ui.common.base

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.github.ajalt.timberkt.d
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class TWBaseDraggableListAdapter<T : Diffable, VH : TWBaseListAdapter.ViewHolder<T>> : TWBaseListAdapter<T, VH>() {
    private lateinit var refList: MutableList<T>

    override fun submitList(list: MutableList<T>?) {
        super.submitList(list)
        refList = list!!
    }

    fun onItemMoved(from: Int, to: Int) {
        refList.add(to, refList.removeAt(from))
        notifyItemMoved(from, to)
    }

    fun activateItemTouchHelper(recyclerView: RecyclerView, onComplete: suspend (itemId: Long, from: Int, to: Int) -> Unit) {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
            private val INVALID_POSITION = -1
            var dragFrom: Int = INVALID_POSITION
            var dragTo: Int = INVALID_POSITION
            var itemId: Long = 0L

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                if (dragFrom == INVALID_POSITION) {
                    dragFrom = fromPosition
                    itemId = recyclerView.adapter!!.getItemId(dragFrom)
                }
                dragTo = toPosition
                d { "Item moving. id=$itemId position={ from=$dragFrom, to=$toPosition }" }
                (recyclerView.adapter as TWBaseDraggableListAdapter<*, *>).onItemMoved(fromPosition, toPosition)
                return true
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                if (dragFrom != INVALID_POSITION && dragTo != INVALID_POSITION && dragFrom != dragTo) {
                    kotlinx.coroutines.GlobalScope.launch(Dispatchers.Main) {
                        onComplete.invoke(itemId, dragFrom, dragTo)
                        dragFrom = INVALID_POSITION
                        dragTo = INVALID_POSITION
                    }
                }
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        }).attachToRecyclerView(recyclerView)
    }
}
