package com.focusblack.wallos.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.focusblack.wallos.R
import com.focusblack.wallos.core.PackRegistry
import com.focusblack.wallos.core.WallpaperEngine
import com.focusblack.wallos.model.Pack
import com.focusblack.wallos.model.Wall
import com.focusblack.wallos.util.ErrorNotifier
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class PackDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PACK_ID = "extra_pack_id"
    }

    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvPackTitle: TextView
    private lateinit var tvPackCount: TextView
    private lateinit var rvWallpapers: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pack_detail)

        toolbar = findViewById(R.id.toolbar)
        tvPackTitle = findViewById(R.id.tv_pack_title)
        tvPackCount = findViewById(R.id.tv_pack_count)
        rvWallpapers = findViewById(R.id.rv_wallpapers)

        // Setup toolbar
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // Get pack from intent
        val packId = intent.getStringExtra(EXTRA_PACK_ID) ?: return finish()
        val pack = PackRegistry.getPack(packId) ?: return finish()

        setupUI(pack)
    }

    private fun setupUI(pack: Pack) {
        // Set pack info
        toolbar.title = pack.title
        tvPackTitle.text = pack.title
        tvPackCount.text = resources.getQuantityString(R.plurals.wallpapers_count, pack.walls.size, pack.walls.size)

        // Setup wallpapers grid (3 columns)
        val adapter = WallpaperAdapter(
            walls = pack.walls,
            scope = lifecycleScope,
            onWallpaperClick = { wall ->
                // Preview full-screen (could open a dialog or new activity)
                previewWallpaper(wall)
            },
            onApplyClick = { wall ->
                applyWallpaper(wall)
            }
        )

        rvWallpapers.layoutManager = GridLayoutManager(this, 2)
        rvWallpapers.adapter = adapter
    }

    private fun previewWallpaper(wall: Wall) {
        // For now, just show a message - could expand to full-screen preview later
        Snackbar.make(rvWallpapers, wall.title, Snackbar.LENGTH_SHORT).show()
    }

    private fun applyWallpaper(wall: Wall) {
        lifecycleScope.launch {
            var failure: WallpaperEngine.ApplyFailure? = null
            val applied = WallpaperEngine.applyWall(this@PackDetailActivity, wall) { error ->
                failure = error
            }
            if (applied) {
                Snackbar.make(rvWallpapers, R.string.wallpaper_applied, Snackbar.LENGTH_SHORT).show()
            } else {
                val message = failure?.userMessage ?: getString(R.string.error_apply_failed)
                ErrorNotifier.showRetrySnackbar(rvWallpapers, message) {
                    applyWallpaper(wall)
                }
            }
        }
    }
}
