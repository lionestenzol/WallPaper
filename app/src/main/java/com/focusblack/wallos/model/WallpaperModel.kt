package com.focusblack.wallos.model

/**
 * Core interface for wallpaper data representation.
 * Supports both local static assets and remote dynamic content.
 */
interface WallpaperModel {
    val id: String
    val title: String
    val remoteUri: String?
    val localDrawableName: String?
    val metadata: WallpaperMetadata
    val syncStatus: SyncStatus
}

/**
 * Metadata associated with wallpaper content.
 * Includes categorization, generation details, and unlock requirements.
 */
data class WallpaperMetadata(
    val category: String = "minimal",
    val tags: List<String> = emptyList(),
    val generatedAt: Long = 0L,
    val generationPrompt: String? = null,
    val resolution: Resolution = Resolution(),
    val releaseEpoch: Long = 0L,
    val unlockRequirement: UnlockRequirement = UnlockRequirement()
)

/**
 * Display resolution information.
 */
data class Resolution(
    val width: Int = 1080,
    val height: Int = 2340,
    val dpi: Int = 480
)

/**
 * Defines how users unlock access to this wallpaper.
 */
data class UnlockRequirement(
    val type: UnlockType = UnlockType.FREE,
    val minStreak: Int = 0,
    val releaseEpochDays: Int = 0
)

/**
 * Types of unlock mechanisms.
 */
enum class UnlockType {
    FREE,       // Available to all users immediately
    STREAK,     // Unlocked after reaching min streak
    PREMIUM,    // Requires IAP (future use)
    TIMED       // Released after N days from releaseEpoch
}

/**
 * Sync status for remote wallpapers.
 * Tracks download and caching state.
 */
enum class SyncStatus {
    PENDING,        // Not yet downloaded
    DOWNLOADING,    // Download in progress
    CACHED,         // Downloaded and cached locally
    FAILED,         // Download failed
    LOCAL_ONLY      // Static local drawable (no remote sync)
}
