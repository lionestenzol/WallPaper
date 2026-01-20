package com.focusblack.wallos.model

/**
 * Wallpaper pack entity with sync tracking.
 * Supports both local static packs and remote dynamic packs.
 *
 * Backward Compatible: Existing code using Pack(id, title, sku, previewImages, walls)
 * continues to work with sensible defaults for local packs.
 */
data class Pack(
    val id: String,
    val title: String,
    val sku: String,
    val previewImages: List<String>,
    val walls: List<Wall>,
    val syncTimestamp: Long = 0L,
    val version: Int = 1,
    val isRemote: Boolean = false,
    val releaseEpoch: Long = 0L,
    val description: String = ""
) {

    /**
     * Returns the number of wallpapers in this pack.
     */
    val wallpaperCount: Int
        get() = walls.size

    /**
     * Returns the number of unlocked wallpapers for a given user streak.
     */
    fun unlockedCount(userStreak: Int): Int {
        return walls.count { wall ->
            when (wall.metadata.unlockRequirement.type) {
                UnlockType.FREE -> true
                UnlockType.STREAK -> userStreak >= wall.metadata.unlockRequirement.minStreak
                UnlockType.PREMIUM -> false // Not implemented yet
                UnlockType.TIMED -> {
                    val daysSinceRelease = (System.currentTimeMillis() / 1000 - wall.metadata.releaseEpoch) / 86400
                    daysSinceRelease >= wall.metadata.unlockRequirement.releaseEpochDays
                }
            }
        }
    }

    /**
     * Checks if this pack needs to be synced from remote.
     */
    fun needsSync(currentTime: Long = System.currentTimeMillis()): Boolean {
        if (!isRemote) return false
        // Sync if older than 24 hours
        val hoursSinceSync = (currentTime - syncTimestamp) / 1000 / 3600
        return hoursSinceSync >= 24
    }

    /**
     * Returns a copy with updated sync timestamp.
     */
    fun withSyncTimestamp(timestamp: Long = System.currentTimeMillis()): Pack {
        return copy(syncTimestamp = timestamp)
    }
}
