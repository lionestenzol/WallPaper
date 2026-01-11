package com.focusblack.wallos.ui

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.focusblack.wallos.R
import com.focusblack.wallos.billing.BillingRepository
import com.focusblack.wallos.core.RotationScheduler
import com.focusblack.wallos.data.OwnershipStore
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var billingRepository: BillingRepository
    private lateinit var ownershipStore: OwnershipStore

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        billingRepository = BillingRepository.getInstance(requireActivity().application)
        ownershipStore = OwnershipStore(requireContext())

        setupAutoRotate()
        setupRotateNow()
        setupProStatus()
        setupRestorePurchases()
        setupUnlockPro()

        // Initialize billing
        lifecycleScope.launch {
            billingRepository.initialize()
            billingRepository.loadProPrice()
            updateProUI()
        }
    }

    override fun onResume() {
        super.onResume()
        updateProUI()
    }

    private fun setupAutoRotate() {
        val autoRotatePref = findPreference<SwitchPreferenceCompat>("auto_rotate")
        autoRotatePref?.setOnPreferenceChangeListener { _, newValue ->
            val enabled = newValue as Boolean
            if (enabled) {
                RotationScheduler.scheduleDailyRotation(requireContext())
            } else {
                RotationScheduler.cancel(requireContext())
            }
            true
        }
    }

    private fun setupRotateNow() {
        val rotateNowPref = findPreference<Preference>("rotate_now")
        rotateNowPref?.setOnPreferenceClickListener {
            RotationScheduler.enqueueOneTimeRotation(requireContext())
            showSnackbar(getString(R.string.pref_rotate_now_queued))
            true
        }
    }

    private fun setupProStatus() {
        updateProUI()
    }

    private fun updateProUI() {
        val proStatusPref = findPreference<Preference>("pro_status")
        val unlockProPref = findPreference<Preference>("unlock_pro")

        val isPro = ownershipStore.isPro()

        proStatusPref?.summary = if (isPro) {
            getString(R.string.pref_pro_status_pro)
        } else {
            getString(R.string.pref_pro_status_free)
        }

        // Hide unlock button if already pro
        unlockProPref?.isVisible = !isPro

        // Update unlock button with price if available
        if (!isPro) {
            val price = billingRepository.getCachedProPrice()
            if (price != null) {
                unlockProPref?.summary = "${getString(R.string.pro_unlock_desc)} - $price"
            }
        }
    }

    private fun setupRestorePurchases() {
        val restorePref = findPreference<Preference>("restore_purchases")
        restorePref?.setOnPreferenceClickListener {
            lifecycleScope.launch {
                showSnackbar(getString(R.string.restoring_purchases))
                val restored = billingRepository.restorePurchases()
                if (restored) {
                    showSnackbar(getString(R.string.restore_success))
                    updateProUI()
                } else {
                    showSnackbar(getString(R.string.restore_nothing))
                }
            }
            true
        }
    }

    private fun setupUnlockPro() {
        val unlockPref = findPreference<Preference>("unlock_pro")
        unlockPref?.setOnPreferenceClickListener {
            if (ownershipStore.isPro()) {
                showSnackbar(getString(R.string.already_pro))
                return@setOnPreferenceClickListener true
            }

            billingRepository.purchasePro(requireActivity()) { success ->
                if (success) {
                    showSnackbar(getString(R.string.purchase_success))
                    updateProUI()
                } else {
                    showSnackbar(getString(R.string.purchase_failed))
                }
            }
            true
        }
    }

    private fun showSnackbar(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show()
        }
    }
}
