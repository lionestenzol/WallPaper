package com.focusblack.wallos.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.focusblack.wallos.R
import com.focusblack.wallos.model.Pack

class PackAdapter(
    private val packs: List<Pack>,
    private val isPro: Boolean,
    private val onPackClick: (Pack) -> Unit
) : RecyclerView.Adapter<PackAdapter.PackViewHolder>() {

    class PackViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivThumb: ImageView = view.findViewById(R.id.iv_pack_thumb)
        val tvTitle: TextView = view.findViewById(R.id.tv_pack_title)
        val tvCount: TextView = view.findViewById(R.id.tv_pack_count)
        val tvProBadge: TextView = view.findViewById(R.id.tv_pro_badge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pack_card, parent, false)
        return PackViewHolder(view)
    }

    override fun onBindViewHolder(holder: PackViewHolder, position: Int) {
        val pack = packs[position]
        val context = holder.itemView.context

        // Set pack info
        holder.tvTitle.text = pack.title
        holder.tvCount.text = context.resources.getQuantityString(R.plurals.wallpapers_count, pack.walls.size, pack.walls.size)

        // Load first wallpaper as thumbnail
        if (pack.walls.isNotEmpty()) {
            val firstWall = pack.walls.first()
            val resourceId = context.resources.getIdentifier(
                firstWall.drawableName,
                "drawable",
                context.packageName
            )
            if (resourceId != 0) {
                val drawable = ContextCompat.getDrawable(context, resourceId)
                holder.ivThumb.setImageDrawable(drawable)
            }
        }

        // Show Pro badge if not pro (for future premium packs)
        // Currently Genesis is free, but this is ready for premium packs
        holder.tvProBadge.visibility = View.GONE

        // Click listener
        holder.itemView.setOnClickListener {
            onPackClick(pack)
        }
    }

    override fun getItemCount(): Int = packs.size
}
