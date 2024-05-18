package dev.pankaj.launchernow

import androidx.recyclerview.widget.DiffUtil

class AppsDiffCallback(
    private val oldList: List<AppInfo>,
    private val newList: List<AppInfo>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].packageName == newList[newItemPosition].packageName
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        return Any()
    }
}