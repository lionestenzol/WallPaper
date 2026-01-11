package com.focusblack.wallos.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.focusblack.wallos.R
import com.focusblack.wallos.data.cache.WallpaperAssetCache
import com.focusblack.wallos.model.Pack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PackAdapter(
    private val packs: List<Pack>,
    private val isPro: Boolean,
    private val scope: CoroutineScope,
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
        val cache = WallpaperAssetCache(context.applicationContext)

        // Set pack info
        holder.tvTitle.text = pack.title
        holder.tvCount.text = context.resources.getQuantityString(R.plurals.wallpapers_count, pack.walls.size, pack.walls.size)

        // Load preview image as thumbnail
        val previewImage = pack.previewImages.firstOrNull()
        holder.ivThumb.tag = previewImage
        if (previewImage.isNullOrBlank()) {
            holder.ivThumb.setImageDrawable(null)
        } else if (previewImage.startsWith("http")) {
            holder.ivThumb.setImageDrawable(null)
            scope.launch {
                val bitmap = cache.loadBitmap(previewImage)
                withContext(Dispatchers.Main) {
                    if (holder.ivThumb.tag == previewImage && bitmap != null) {
                        holder.ivThumb.setImageBitmap(bitmap)
                    }
                }
            }
        } else {
            val resourceId = context.resources.getIdentifier(
                previewImage,
                "drawable",
                context.packageName
            )
            if (resourceId != 0) {
                val drawable = ContextCompat.getDrawable(context, resourceId)
                holder.ivThumb.setImageDrawable(drawable)
            } else {
                holder.ivThumb.setImageDrawable(null)
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
