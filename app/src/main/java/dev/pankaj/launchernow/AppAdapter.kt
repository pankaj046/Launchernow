package dev.pankaj.launchernow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class AppAdapter(
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {
   var apps: List<AppInfo> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]
        holder.bind(app)
    }

    override fun getItemCount() = apps.size

    fun updateData(apps: List<AppInfo>) {
        val diffResult = DiffUtil.calculateDiff(AppsDiffCallback(this.apps, apps))
        this.apps = apps
        diffResult.dispatchUpdatesTo(this)
    }

    inner class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nameTextView: TextView = view.findViewById(R.id.title)
        private val iconImageView: ImageView = view.findViewById(R.id.icon)
        init {
            itemView.setOnClickListener {
                onClick(apps[absoluteAdapterPosition].packageName)
            }
        }

        fun bind(app: AppInfo) {
            nameTextView.text = app.name
            iconImageView.setImageDrawable(app.icon)
        }
    }
}
