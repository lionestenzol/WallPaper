package com.focusblack.wallos.core

interface AnalyticsTracker {
    fun track(event: String, attributes: Map<String, Any?> = emptyMap())
}

object WallosAnalytics {
    @Volatile
    var tracker: AnalyticsTracker? = null

    fun track(event: String, attributes: Map<String, Any?> = emptyMap()) {
        tracker?.track(event, attributes)
    }
}
