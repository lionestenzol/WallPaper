package com.focusblack.wallos.core

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object RotationScheduler {
    private const val WORK_NAME = "focusblack_daily_rotation"
    private const val ONE_TIME_WORK_NAME = "focusblack_manual_rotation"
    private const val KEY_ROTATION_CONSTRAINTS = "rotation_constraints"

    fun scheduleDailyRotation(context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val constraintsEnabled = prefs.getBoolean(KEY_ROTATION_CONSTRAINTS, true)
        val constraints = if (constraintsEnabled) {
            Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiresCharging(false)
                .build()
        } else {
            Constraints.NONE
        }
        val work = PeriodicWorkRequestBuilder<RotationWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, work)
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    fun enqueueOneTimeRotation(context: Context) {
        val work = OneTimeWorkRequestBuilder<RotationWorker>().build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(ONE_TIME_WORK_NAME, ExistingWorkPolicy.REPLACE, work)
    }
}
