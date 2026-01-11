package com.focusblack.wallos.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.focusblack.wallos.R
import com.focusblack.wallos.core.PackRegistry
import com.focusblack.wallos.data.OwnershipStore

class PacksFragment : Fragment() {

    private lateinit var rvPacks: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_packs, container, false)

        rvPacks = view.findViewById(R.id.rv_packs)

        setupRecyclerView()

        return view
    }

    override fun onResume() {
        super.onResume()
        // Refresh in case pro status changed
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val ownershipStore = OwnershipStore(requireContext())
        val isPro = ownershipStore.isPro()

        val packs = PackRegistry.listPacks()

        val adapter = PackAdapter(packs, isPro, viewLifecycleOwner.lifecycleScope) { pack ->
            // Open pack detail
            val intent = Intent(requireContext(), PackDetailActivity::class.java)
            intent.putExtra(PackDetailActivity.EXTRA_PACK_ID, pack.id)
            startActivity(intent)
        }

        rvPacks.layoutManager = LinearLayoutManager(requireContext())
        rvPacks.adapter = adapter
    }
}
