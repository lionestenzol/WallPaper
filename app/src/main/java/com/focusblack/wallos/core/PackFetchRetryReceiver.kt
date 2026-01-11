package com.focusblack.wallos.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PackFetchRetryReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_RETRY_PACKS) {
            return
        }
        val url = intent.getStringExtra(EXTRA_URL)
        if (url.isNullOrBlank()) {
            Log.e(TAG, "Retry pack fetch failed: missing url")
            return
        }
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            PackRegistry.loadRemotePacks(context, url)
            pendingResult.finish()
        }
    }

    companion object {
        private const val TAG = "PackFetchRetryReceiver"
        const val ACTION_RETRY_PACKS = "com.focusblack.wallos.action.RETRY_PACK_FETCH"
        const val EXTRA_URL = "extra_url"

        fun createIntent(context: Context, url: String): Intent {
            return Intent(context, PackFetchRetryReceiver::class.java).apply {
                action = ACTION_RETRY_PACKS
                putExtra(EXTRA_URL, url)
            }
        }
    }
}
