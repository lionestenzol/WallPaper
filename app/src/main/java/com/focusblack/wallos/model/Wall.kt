package com.focusblack.wallos.model

/**
 * Wallpaper entity implementing WallpaperModel.
 * Supports both local static drawables and remote dynamic content.
 *
 * Backward Compatible: Existing code using Wall(id, title, drawableName) continues to work.
 * All new fields have sensible defaults for local-only wallpapers.
 */
data class Wall(
    override val id: String,
    override val title: String,
    val drawableName: String,
    val assetUrl: String? = null,
    override val metadata: WallpaperMetadata = WallpaperMetadata(),
    override val syncStatus: SyncStatus = if (assetUrl != null) SyncStatus.PENDING else SyncStatus.LOCAL_ONLY
) : WallpaperModel {

    /**
     * Remote URI for cloud-hosted wallpapers.
     * Maps to assetUrl for backward compatibility.
     */
    override val remoteUri: String?
        get() = assetUrl

    /**
     * Local drawable resource name.
     * Used when syncStatus is LOCAL_ONLY or as fallback.
     */
    override val localDrawableName: String?
        get() = drawableName

    /**
     * Convenience property to check if this is a remote wallpaper.
     */
    val isRemote: Boolean
        get() = remoteUri != null

    /**
     * Convenience property to check if this wallpaper is cached and ready.
     */
    val isReady: Boolean
        get() = syncStatus == SyncStatus.CACHED || syncStatus == SyncStatus.LOCAL_ONLY
}
