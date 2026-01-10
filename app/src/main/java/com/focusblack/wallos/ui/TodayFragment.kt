package com.focusblack.wallos.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
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
            // apply the first genesis wall
            val pack = PackRegistry.getPack("GENESIS_001")
            val wall = pack?.walls?.firstOrNull()
            if (wall != null) {
                com.focusblack.wallos.core.WallpaperEngine.applyWall(requireContext(), wall)
                StreakEngine.onDailyApplied(requireContext())
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
