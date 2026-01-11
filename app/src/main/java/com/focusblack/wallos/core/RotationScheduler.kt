package com.focusblack.wallos.core

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object RotationScheduler {
    private const val WORK_NAME = "focusblack_daily_rotation"
    private const val ONE_TIME_WORK_NAME = "focusblack_manual_rotation"

    fun scheduleDailyRotation(context: Context) {
        val work = PeriodicWorkRequestBuilder<RotationWorker>(1, TimeUnit.DAYS)
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
