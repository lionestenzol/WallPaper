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
import com.focusblack.wallos.model.Wall
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WallpaperAdapter(
    private val walls: List<Wall>,
    private val scope: CoroutineScope,
    private val onWallpaperClick: (Wall) -> Unit,
    private val onApplyClick: (Wall) -> Unit
) : RecyclerView.Adapter<WallpaperAdapter.WallpaperViewHolder>() {

    class WallpaperViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivThumb: ImageView = view.findViewById(R.id.iv_wall_thumb)
        val tvTitle: TextView = view.findViewById(R.id.tv_wall_title)
        val btnApply: MaterialButton = view.findViewById(R.id.btn_apply)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WallpaperViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wallpaper_thumb, parent, false)
        return WallpaperViewHolder(view)
    }

    override fun onBindViewHolder(holder: WallpaperViewHolder, position: Int) {
        val wall = walls[position]
        val context = holder.itemView.context
        val cache = WallpaperAssetCache(context.applicationContext)

        // Set wall info
        holder.tvTitle.text = wall.title

        // Load wallpaper thumbnail
        holder.ivThumb.tag = wall.assetUrl ?: wall.drawableName
        val assetUrl = wall.assetUrl
        if (!assetUrl.isNullOrBlank()) {
            holder.ivThumb.setImageDrawable(null)
            scope.launch {
                val bitmap = cache.loadBitmap(assetUrl)
                withContext(Dispatchers.Main) {
                    if (holder.ivThumb.tag == assetUrl && bitmap != null) {
                        holder.ivThumb.setImageBitmap(bitmap)
                    }
                }
            }
        } else {
            val resourceId = context.resources.getIdentifier(
                wall.drawableName,
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

        // Click to preview full-screen
        holder.itemView.setOnClickListener {
            onWallpaperClick(wall)
        }

        // Apply button
        holder.btnApply.setOnClickListener {
            onApplyClick(wall)
        }
    }

    override fun getItemCount(): Int = walls.size
}
