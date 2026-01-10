package com.focusblack.wallos.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.focusblack.wallos.R
import com.focusblack.wallos.core.PackRegistry

class PacksFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_packs, container, false)
        val containerLl = view.findViewById<LinearLayout>(R.id.packs_container)
        containerLl.removeAllViews()
        for (pack in PackRegistry.listPacks()) {
            val tv = TextView(requireContext())
            tv.text = "${pack.title} - ${pack.walls.size} wallpapers"
            tv.setPadding(16, 16, 16, 16)
            tv.textSize = 16f
            containerLl.addView(tv)
        }
        return view
    }
}
