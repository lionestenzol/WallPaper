package com.focusblack.wallos.ui

import android.content.Context
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import com.focusblack.wallos.R
import com.focusblack.wallos.core.RotationScheduler
import com.focusblack.wallos.core.WallpaperEngine
import com.google.android.material.snackbar.Snackbar
import java.text.DateFormat
import java.util.Date

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        setupAutoRotate()
        setupRotateNow()
        setupApplyTarget()
        setupRotationConstraints()
    }

    override fun onResume() {
        super.onResume()
        updateLastApplyStatus()
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

    private fun setupApplyTarget() {
        val applyTargetPref = findPreference<ListPreference>(PREF_APPLY_TARGET)
        applyTargetPref?.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
    }

    private fun setupRotationConstraints() {
        val constraintsPref = findPreference<SwitchPreferenceCompat>("rotation_constraints")
        constraintsPref?.setOnPreferenceChangeListener { _, _ ->
            val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
            val autoRotateEnabled = prefs.getBoolean("auto_rotate", true)
            if (autoRotateEnabled) {
                RotationScheduler.scheduleDailyRotation(requireContext())
            }
            true
        }
    }


    private fun updateLastApplyStatus() {
        val lastApplyPref = findPreference<Preference>("wallpaper_last_apply_status")
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val lastApplyTime = prefs.getLong(WallpaperEngine.KEY_LAST_APPLY_TIME, 0L)

        if (lastApplyTime == 0L) {
            lastApplyPref?.summary = getString(R.string.pref_last_apply_status_none)
            return
        }

        val lastApplySuccess = prefs.getBoolean(WallpaperEngine.KEY_LAST_APPLY_RESULT, false)
        val lastApplyError = prefs.getString(WallpaperEngine.KEY_LAST_APPLY_ERROR, null)
        val statusLabel = if (lastApplySuccess) {
            getString(R.string.pref_last_apply_status_success)
        } else if (!lastApplyError.isNullOrBlank()) {
            getString(R.string.pref_last_apply_status_failure_with_reason, lastApplyError)
        } else {
            getString(R.string.pref_last_apply_status_failure)
        }
        val timeLabel = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
            .format(Date(lastApplyTime))
        lastApplyPref?.summary = getString(R.string.pref_last_apply_status_summary_fmt, statusLabel, timeLabel)
    }

    private fun showSnackbar(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val PREF_APPLY_TARGET = "apply_target"

        fun getApplyTarget(context: Context): WallpaperEngine.ApplyTarget {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val value = prefs.getString(PREF_APPLY_TARGET, WallpaperEngine.ApplyTarget.BOTH.prefValue)
            return WallpaperEngine.ApplyTarget.fromPreference(value)
        }
    }
}
