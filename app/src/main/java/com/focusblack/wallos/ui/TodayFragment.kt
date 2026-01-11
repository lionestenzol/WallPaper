package com.focusblack.wallos.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.focusblack.wallos.R
import com.focusblack.wallos.core.PackRegistry
import com.focusblack.wallos.core.StreakEngine
import com.focusblack.wallos.core.WallpaperEngine
import com.focusblack.wallos.data.ReviewGate
import com.focusblack.wallos.model.Wall
import com.focusblack.wallos.util.ReviewHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TodayFragment : Fragment() {

    private lateinit var ivPreview: ImageView
    private lateinit var tvWallTitle: TextView
    private lateinit var tvPackInfo: TextView
    private lateinit var tvStreak: TextView
    private lateinit var btnApply: MaterialButton
    private lateinit var progressApply: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_today, container, false)

        ivPreview = view.findViewById(R.id.iv_preview)
        tvWallTitle = view.findViewById(R.id.tv_wall_title)
        tvPackInfo = view.findViewById(R.id.tv_pack_info)
        tvStreak = view.findViewById(R.id.tv_streak)
        btnApply = view.findViewById(R.id.btn_apply)
        progressApply = view.findViewById(R.id.progress_apply)

        val reviewGate = ReviewGate(requireContext())

        btnApply.setOnClickListener {
            applyCurrentWallpaper(reviewGate)
        }

        refreshUI()
        return view
    }

    override fun onResume() {
        super.onResume()
        refreshUI()
    }

    private fun refreshUI() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val pack = PackRegistry.getPack("GENESIS_001") ?: return

        val currentIndex = prefs.getInt(KEY_CURRENT_INDEX, 0)
        val wall = pack.walls.getOrNull(currentIndex) ?: pack.walls.first()

        // Update preview
        loadWallpaperPreview(wall)

        // Update wallpaper info
        tvWallTitle.text = wall.title
        tvPackInfo.text = getString(R.string.pack_info_fmt, pack.title, currentIndex + 1, pack.walls.size)

        // Update streak
        val streak = StreakEngine.getStreak(requireContext())
        tvStreak.text = streak.toString()
    }

    private fun loadWallpaperPreview(wall: Wall) {
        val resourceId = requireContext().resources.getIdentifier(
            wall.drawableName,
            "drawable",
            requireContext().packageName
        )

        if (resourceId != 0) {
            val drawable = ContextCompat.getDrawable(requireContext(), resourceId)
            ivPreview.setImageDrawable(drawable)
        }
    }

    private fun applyCurrentWallpaper(reviewGate: ReviewGate) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val pack = PackRegistry.getPack("GENESIS_001") ?: return

        val currentIndex = prefs.getInt(KEY_CURRENT_INDEX, 0)
        val wall = pack.walls.getOrNull(currentIndex) ?: return

        // Show loading
        btnApply.isEnabled = false
        progressApply.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
            val applied = withContext(Dispatchers.IO) {
                WallpaperEngine.applyWall(requireContext(), wall)
            }
            ensureActive()
            if (applied && isAdded && isActive) {
                StreakEngine.onDailyApplied(requireContext())

                // Advance to next wallpaper
                val nextIndex = (currentIndex + 1) % pack.walls.size
                prefs.edit { putInt(KEY_CURRENT_INDEX, nextIndex) }

                // Review gate
                reviewGate.recordApply()
                if (reviewGate.shouldShowReview()) {
                    withContext(Dispatchers.Main) {
                        if (isAdded && isActive) {
                            ReviewHelper.showReviewIfAppropriate(requireActivity())
                            reviewGate.setShown()
                        }
                    }
                }
            }

            withContext(Dispatchers.Main) {
                if (!isAdded || !isActive) {
                    return@withContext
                }
                // Hide loading and show result
                progressApply.visibility = View.GONE
                btnApply.isEnabled = true

                view?.let {
                    val message = if (applied) {
                        R.string.wallpaper_applied
                    } else {
                        R.string.error_apply_failed
                    }
                    Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show()
                }

                if (applied) {
                    // Update UI to show next wallpaper
                    refreshUI()
                }
            }
        }
    }

    companion object {
        private const val KEY_CURRENT_INDEX = "rotation_current_index"
    }
}
