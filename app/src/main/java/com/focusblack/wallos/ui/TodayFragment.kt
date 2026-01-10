package com.focusblack.wallos.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.focusblack.wallos.R
import com.focusblack.wallos.core.PackRegistry
import com.focusblack.wallos.core.StreakEngine
import com.focusblack.wallos.data.ReviewGate
import com.focusblack.wallos.util.ReviewHelper

class TodayFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_today, container, false)
        val applyBtn = view.findViewById<Button>(R.id.btn_apply)
        val streakTv = view.findViewById<TextView>(R.id.tv_streak)

        val reviewGate = ReviewGate(requireContext())

        fun refreshStreak() {
            val s = StreakEngine.getStreak(requireContext())
            streakTv.text = getString(R.string.streak_fmt, s)
        }

        applyBtn.setOnClickListener {
            val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
            val pack = PackRegistry.getPack("GENESIS_001")

            if (pack != null && pack.walls.isNotEmpty()) {
                // Get current wallpaper index and cycle to next
                val currentIndex = prefs.getInt("rotation_current_index", 0)
                val nextIndex = (currentIndex + 1) % pack.walls.size
                val wall = pack.walls[currentIndex]

                // Apply wallpaper
                com.focusblack.wallos.core.WallpaperEngine.applyWall(requireContext(), wall)
                StreakEngine.onDailyApplied(requireContext())

                // Save next index
                prefs.edit().putInt("rotation_current_index", nextIndex).apply()

                // Review gate
                reviewGate.recordApply()
                if (reviewGate.shouldShowReview()) {
                    ReviewHelper.showReviewIfAppropriate(requireActivity())
                    reviewGate.setShown()
                }

                refreshStreak()
            }
        }

        refreshStreak()
        return view
    }
}
