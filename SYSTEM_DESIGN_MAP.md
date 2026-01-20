# System Design Map: Cloud-Native Content Pipeline Refactoring

**Document Version**: 1.0
**Date**: 2026-01-20
**Target**: Lead Developer Agent
**From**: System Architect Analysis

---

## Executive Summary

This document provides a comprehensive architectural analysis and integration map for transforming the focus-black wallpaper application from a static asset model to a dynamic, cloud-native content pipeline with AI-generated wallpapers.

**Current State**: Static local drawable resources (7 wallpapers)
**Target State**: Dynamic remote content with AI generation pipeline
**Approach**: Phased refactoring with backward compatibility

---

## Phase 1: Data Layer Decoupling & Repository Pattern

### 1.1 Current Architecture Analysis

#### Current Data Flow
```
PackRegistry (static) → Wall (model) → WallpaperEngine → System Wallpaper Manager
```

#### Critical Files & Integration Points

**File**: `/app/src/main/java/com/focusblack/wallos/model/Wall.kt`
- **Current State**: Lines 1-8
- **Status**: ✓ Already has optional `assetUrl` field (line 7)
- **Action Required**: Extend with metadata fields

**File**: `/app/src/main/java/com/focusblack/wallos/model/Pack.kt`
- **Current State**: Lines 1-9
- **Status**: Basic model without metadata
- **Action Required**: Add sync metadata fields

**File**: `/app/src/main/java/com/focusblack/wallos/core/PackRegistry.kt`
- **Current State**: Lines 7-45
- **Integration Points**:
  - Line 8: Static `mutableMapOf<String, Pack>()` - needs persistence layer
  - Lines 10-28: Hardcoded pack initialization - needs abstraction
  - Lines 34-38: `upsertPacks()` method - ✓ Already supports remote packs
  - Lines 40-44: `loadRemotePacks()` method - ✓ Already has remote loading infrastructure

**File**: `/app/src/main/java/com/focusblack/wallos/data/PackRepository.kt`
- **Current State**: Lines 1-59
- **Integration Points**:
  - Line 14: `fetchRemotePacks()` - uses basic HttpURLConnection
  - Lines 24-54: JSON parsing logic - needs schema extension
  - **Action Required**: Replace with Firebase/Supabase SDK

### 1.2 Proposed New Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     UI Layer                                 │
│  TodayFragment, PacksFragment (No changes required)         │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                  Business Logic Layer                        │
│  PackRegistry → WallpaperRepository (NEW)                   │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    Repository Layer (NEW)                    │
│  WallpaperRepository                                        │
│    ├─ RemoteDataSource (Firebase/Supabase)                 │
│    ├─ LocalCacheSource (Room/SQLite)                       │
│    └─ SyncEngine (Background sync)                         │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                   Data Sources                               │
│  Firebase Firestore / Supabase PostgreSQL                   │
│  Firebase Storage / Supabase Storage                        │
└─────────────────────────────────────────────────────────────┘
```

### 1.3 Model Extensions Required

#### New WallpaperModel Interface

**New File**: `/app/src/main/java/com/focusblack/wallos/model/WallpaperModel.kt`
```kotlin
interface WallpaperModel {
    val id: String
    val title: String
    val remoteUri: String?
    val localDrawableName: String?
    val metadata: WallpaperMetadata
    val syncStatus: SyncStatus
}

data class WallpaperMetadata(
    val category: String,
    val tags: List<String>,
    val generatedAt: Long,
    val generationPrompt: String?,
    val resolution: Resolution,
    val releaseEpoch: Long
)

data class Resolution(
    val width: Int,
    val height: Int,
    val dpi: Int
)

enum class SyncStatus {
    PENDING,        // Not yet downloaded
    DOWNLOADING,    // Download in progress
    CACHED,         // Downloaded and cached
    FAILED,         // Download failed
    LOCAL_ONLY      // Static local drawable
}
```

**Integration Point**: Existing `Wall.kt` (lines 1-8) should implement `WallpaperModel`

#### Pack Model Extension

**File**: `/app/src/main/java/com/focusblack/wallos/model/Pack.kt`

**Current**: Lines 3-9
**Modification Required**: Add sync metadata
```kotlin
data class Pack(
    val id: String,
    val title: String,
    val sku: String,
    val previewImages: List<String>,
    val walls: List<Wall>,
    // NEW FIELDS BELOW
    val syncTimestamp: Long = 0L,
    val version: Int = 1,
    val isRemote: Boolean = false,
    val releaseEpoch: Long = 0L
)
```

---

## Phase 2: Dynamic Sync Engine Implementation

### 2.1 New Repository Layer

#### WallpaperRepository Implementation

**New File**: `/app/src/main/java/com/focusblack/wallos/data/repository/WallpaperRepository.kt`

**Purpose**: Central data access point with cache-first strategy

**Key Responsibilities**:
- Fetch remote pack metadata from Firestore/Supabase
- Coordinate with ImageCacheManager for asset downloads
- Handle cache invalidation and sync state
- Provide Observable/Flow for UI reactivity

**Integration Points**:
- **Replaces**: `PackRegistry.loadRemotePacks()` (PackRegistry.kt:40-44)
- **Used by**: `PackRegistry` (becomes a facade over WallpaperRepository)

#### Remote Data Source Options

**Option A: Firebase Firestore**

**New File**: `/app/src/main/java/com/focusblack/wallos/data/remote/FirebaseDataSource.kt`

**Gradle Dependency Addition** (`app/build.gradle` line 43):
```gradle
// Firebase
implementation platform('com.google.firebase:firebase-bom:33.7.0')
implementation 'com.google.firebase:firebase-firestore-ktx'
implementation 'com.google.firebase:firebase-storage-ktx'
```

**Firestore Schema**:
```
/packs/{packId}
  - id: String
  - title: String
  - sku: String
  - releaseEpoch: Long
  - version: Int
  - previewImages: Array<String>

/packs/{packId}/wallpapers/{wallId}
  - id: String
  - title: String
  - storageUri: String (gs://bucket/path)
  - metadata: Map<String, Any>
  - releaseEpoch: Long
  - category: String
  - tags: Array<String>
  - resolution: {width, height, dpi}
```

**Option B: Supabase**

**New File**: `/app/src/main/java/com/focusblack/wallos/data/remote/SupabaseDataSource.kt`

**Gradle Dependency Addition** (`app/build.gradle` line 43):
```gradle
// Supabase
implementation 'io.github.jan-tennert.supabase:postgrest-kt:3.0.2'
implementation 'io.github.jan-tennert.supabase:storage-kt:3.0.2'
implementation 'io.github.jan-tennert.supabase:realtime-kt:3.0.2'
implementation 'io.ktor:ktor-client-android:3.0.3'
```

**PostgreSQL Schema**:
```sql
CREATE TABLE packs (
  id TEXT PRIMARY KEY,
  title TEXT NOT NULL,
  sku TEXT NOT NULL,
  release_epoch BIGINT NOT NULL,
  version INTEGER DEFAULT 1,
  preview_images JSONB,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE wallpapers (
  id TEXT PRIMARY KEY,
  pack_id TEXT REFERENCES packs(id),
  title TEXT NOT NULL,
  storage_uri TEXT NOT NULL,
  category TEXT,
  tags JSONB,
  generation_prompt TEXT,
  resolution JSONB,
  release_epoch BIGINT NOT NULL,
  created_at TIMESTAMPTZ DEFAULT NOW()
);
```

### 2.2 Image Cache Manager

**File**: `/app/src/main/java/com/focusblack/wallos/data/cache/WallpaperAssetCache.kt`

**Current State**: Lines 1-49
**Status**: ✓ Already has download + disk cache infrastructure
**Integration Points**:
  - Line 21: `getOrDownload()` method - ✓ Already supports remote URLs
  - Lines 16-19: DiskLruCache with 100MB limit - ✓ Already configured
  - **Enhancement Required**: Add priority queue for scroll-optimization

**New File**: `/app/src/main/java/com/focusblack/wallos/data/cache/ImageCacheManager.kt`

**Purpose**: High-performance image buffer management with scroll-event optimization

**Key Features**:
- Priority queue based on UI visibility
- Prefetch next N wallpapers in rotation
- Memory-efficient Bitmap pooling
- Cancel pending downloads on scroll events

**Integration Hook**:
- **Called by**: `TodayFragment.loadWallpaperPreview()` (TodayFragment.kt:87-112)
- **Wraps**: Existing `WallpaperAssetCache` (maintains backward compatibility)

**Enhancement to Existing Cache**:

**File**: `/app/src/main/java/com/focusblack/wallos/data/cache/WallpaperAssetCache.kt`

**Modification Point**: After line 19, add priority-aware download queue
```kotlin
private val downloadQueue = PriorityBlockingQueue<DownloadRequest>()
private val activeDownloads = ConcurrentHashMap<String, Job>()

data class DownloadRequest(
    val url: String,
    val priority: DownloadPriority,
    val timestamp: Long = System.currentTimeMillis()
) : Comparable<DownloadRequest> {
    override fun compareTo(other: DownloadRequest): Int {
        return compareValuesBy(this, other,
            { -it.priority.ordinal },
            { it.timestamp }
        )
    }
}

enum class DownloadPriority {
    IMMEDIATE,  // User tapped apply
    HIGH,       // Visible in viewport
    MEDIUM,     // Next in rotation
    LOW         // Prefetch
}
```

### 2.3 Sync Engine Worker

**New File**: `/app/src/main/java/com/focusblack/wallos/core/SyncWorker.kt`

**Purpose**: Background sync for new wallpaper packs

**Integration Point**: Schedule alongside rotation worker

**File**: `/app/src/main/java/com/focusblack/wallos/core/RotationScheduler.kt`

**Current Location**: (Need to find this file - likely exists given RotationWorker)

**Modification Required**: Add sync scheduling
```kotlin
fun scheduleSync(context: Context) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.UNMETERED)  // WiFi only
        .setRequiresBatteryNotLow(true)
        .build()

    val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
        repeatInterval = 12,
        repeatIntervalTimeUnit = TimeUnit.HOURS
    )
        .setConstraints(constraints)
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "wallpaper_sync",
        ExistingPeriodicWorkPolicy.KEEP,
        syncRequest
    )
}
```

---

## Phase 3: Autonomous Content Pipeline (The "Engine")

### 3.1 Architecture Overview

```
┌────────────────────────────────────────────────────────────┐
│            Content Automation Script (External)            │
│                   Python/Node.js Service                   │
└────────────────────────────────────────────────────────────┘
                           ↓
┌────────────────────────────────────────────────────────────┐
│                  Stability AI / SDXL API                   │
│                  (Image Generation)                        │
└────────────────────────────────────────────────────────────┘
                           ↓
┌────────────────────────────────────────────────────────────┐
│                 Upscaling Service (Optional)               │
│               Real-ESRGAN / ESRGAN / Topaz                │
└────────────────────────────────────────────────────────────┘
                           ↓
┌────────────────────────────────────────────────────────────┐
│              Cloud Storage (Firebase/Supabase)            │
│                    Image Hosting                           │
└────────────────────────────────────────────────────────────┘
                           ↓
┌────────────────────────────────────────────────────────────┐
│            Database (Firestore/PostgreSQL)                 │
│                  Metadata Registry                         │
└────────────────────────────────────────────────────────────┘
```

### 3.2 ContentAutomator Script

**New File**: `/automation/content_automator.py` (or `.js`)

**Not in Android codebase - external service/script**

**Key Components**:

#### 3.2.1 Prompt Engineering Template System

```python
# Example structure
templates = {
    "minimal_geometric": [
        "pure black background, single {shape} in {position}, minimal AMOLED wallpaper",
        "black (#000000) wallpaper, geometric {shape}, centered, ultra minimal"
    ],
    "abstract_dark": [
        "dark abstract {pattern}, OLED optimized, {color_accent} accent on black",
        "minimalist {pattern} on pure black, battery-saving wallpaper"
    ]
}

categories = ["geometric", "abstract", "nature", "technology"]
shapes = ["circle", "triangle", "line", "grid", "hexagon"]
positions = ["center", "bottom-third", "golden-ratio"]
```

**Style Requirements**:
- Pure black (#000000) background (AMOLED optimization)
- Minimal colored elements (<10% of canvas)
- High contrast for visibility
- Device-native aspect ratios (9:16, 9:18, 9:19.5, 9:20)

#### 3.2.2 Generation Pipeline Logic

**Pseudocode Flow**:
```
1. SELECT template from category based on schedule
2. GENERATE variations using template system
3. CALL Stability AI API with prompt + parameters
4. VALIDATE generation (check for black background, minimal elements)
5. IF valid:
     a. UPSCALE to target resolution (2160x4680 or device-specific)
     b. OPTIMIZE file size (WebP conversion)
     c. UPLOAD to cloud storage
     d. INSERT metadata to database
     e. LOG generation record
6. ELSE:
     RETRY with adjusted prompt
```

#### 3.2.3 Stability AI Integration

**API Endpoint**: `https://api.stability.ai/v2beta/stable-image/generate/sd3`

**Parameters**:
```json
{
  "prompt": "{generated_prompt}",
  "negative_prompt": "colorful, bright, white background, text, watermark",
  "aspect_ratio": "9:19.5",
  "style_preset": "dark",
  "output_format": "png",
  "cfg_scale": 7,
  "steps": 30,
  "seed": 0
}
```

**Authentication**: API key in environment variable

#### 3.2.4 Resolution Normalization & Upscaling

**Target Resolutions**:
- **Base**: 1080x2340 (FHD+, 9:19.5)
- **High**: 1440x3120 (QHD+, 9:19.5)
- **Ultra**: 2160x4680 (4K, 9:19.5)

**Upscaling Pipeline**:
```python
def upscale_to_device_native(image_path: str, target_resolution: tuple):
    # Option 1: Real-ESRGAN (open source)
    subprocess.run([
        "realesrgan-ncnn-vulkan",
        "-i", image_path,
        "-o", output_path,
        "-s", scale_factor
    ])

    # Option 2: Stability AI Upscale API
    response = requests.post(
        "https://api.stability.ai/v2beta/stable-image/upscale/conservative",
        files={"image": open(image_path, "rb")},
        data={"output_format": "webp"}
    )
```

#### 3.2.5 Cloud Storage Upload

**Firebase Storage**:
```python
import firebase_admin
from firebase_admin import storage

bucket = storage.bucket()
blob = bucket.blob(f"wallpapers/{pack_id}/{wallpaper_id}.webp")
blob.upload_from_filename(local_path)
blob.make_public()
storage_uri = blob.public_url
```

**Supabase Storage**:
```python
from supabase import create_client

supabase = create_client(url, key)
response = supabase.storage.from_("wallpapers").upload(
    path=f"{pack_id}/{wallpaper_id}.webp",
    file=open(local_path, "rb")
)
storage_uri = supabase.storage.from_("wallpapers").get_public_url(path)
```

#### 3.2.6 Automated Database POST

**Firestore**:
```python
from firebase_admin import firestore

db = firestore.client()
doc_ref = db.collection("packs").document(pack_id).collection("wallpapers").document(wallpaper_id)
doc_ref.set({
    "id": wallpaper_id,
    "title": generated_title,
    "storageUri": storage_uri,
    "metadata": {
        "category": category,
        "tags": tags,
        "generationPrompt": prompt,
        "generatedAt": firestore.SERVER_TIMESTAMP
    },
    "resolution": {"width": 2160, "height": 4680, "dpi": 480},
    "releaseEpoch": int(time.time())
})
```

**Supabase**:
```python
data = {
    "id": wallpaper_id,
    "pack_id": pack_id,
    "title": generated_title,
    "storage_uri": storage_uri,
    "category": category,
    "tags": tags,
    "generation_prompt": prompt,
    "resolution": {"width": 2160, "height": 4680, "dpi": 480},
    "release_epoch": int(time.time())
}
supabase.table("wallpapers").insert(data).execute()
```

### 3.3 Android App Integration Points

**No code changes required in Android app for Phase 3!**

The app already has all necessary infrastructure:
- ✓ Remote pack fetching (PackRegistry.kt:40-44)
- ✓ Remote image loading (WallpaperEngine.kt:128-136)
- ✓ Asset caching (WallpaperAssetCache.kt:21-36)

**Only requirement**: Update remote data source URL to point to new Firebase/Supabase endpoint

---

## Phase 4: Streak-Gating Logic

### 4.1 Current Streak Implementation Analysis

**File**: `/app/src/main/java/com/focusblack/wallos/core/StreakEngine.kt`

**Current State**: Lines 1-35
**Integration Points**:
  - Lines 9-11: Local SharedPreferences keys
  - Lines 13-29: `onDailyApplied()` - purely local calculation
  - Lines 31-34: `getStreak()` - purely local read
  - **No remote sync capability**

**Used By**:
- `TodayFragment.applyCurrentWallpaper()` (TodayFragment.kt:138)
- `RotationWorker.doWork()` (RotationWorker.kt:69)

### 4.2 Proposed Remote Streak Architecture

```
┌────────────────────────────────────────────────────────────┐
│                   Android Client                           │
│  StreakEngine → StreakRepository → RemoteDataSource        │
└────────────────────────────────────────────────────────────┘
                           ↓
┌────────────────────────────────────────────────────────────┐
│              Remote Database (Firestore/Supabase)         │
│  User Profile: {userId, streak, lastCheckIn, wallsUnlocked}│
└────────────────────────────────────────────────────────────┘
                           ↓
┌────────────────────────────────────────────────────────────┐
│                 Content Gating Rules                       │
│  IF streak >= wallpaper.releaseEpoch THEN unlock          │
└────────────────────────────────────────────────────────────┘
```

### 4.3 Database Schema Extension

**Firestore**:
```
/users/{userId}
  - userId: String (Firebase Auth UID)
  - streak: Number
  - lastCheckInDate: Timestamp
  - totalApplies: Number
  - unlockedWallpapers: Array<String>
  - createdAt: Timestamp

/wallpapers/{wallId}
  - ...existing fields...
  - unlockRequirement: {
      type: "streak" | "free" | "premium",
      minStreak: Number,
      releaseEpoch: Long
    }
```

**Supabase**:
```sql
CREATE TABLE user_profiles (
  user_id UUID PRIMARY KEY REFERENCES auth.users(id),
  streak INTEGER DEFAULT 0,
  last_check_in TIMESTAMPTZ,
  total_applies INTEGER DEFAULT 0,
  unlocked_wallpapers JSONB DEFAULT '[]',
  created_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE wallpapers ADD COLUMN unlock_requirement JSONB;

-- Example unlock_requirement:
-- {"type": "streak", "minStreak": 7, "releaseEpoch": 1737331200}
```

### 4.4 StreakEngine Refactoring

**File**: `/app/src/main/java/com/focusblack/wallos/core/StreakEngine.kt`

**Modification Strategy**: Maintain existing interface, add remote sync

**New Method Additions** (after line 34):
```kotlin
suspend fun syncWithRemote(context: Context, userId: String) {
    // Fetch remote streak from Firestore/Supabase
    // Merge local and remote (take max)
    // Update both local and remote
}

suspend fun checkInDaily(context: Context, userId: String): CheckInResult {
    // Called when wallpaper applied
    // POST to remote endpoint
    // Update remote profile with new streak
    // Return unlocked content
}

data class CheckInResult(
    val newStreak: Int,
    val newlyUnlockedWallpapers: List<String>
)
```

**New File**: `/app/src/main/java/com/focusblack/wallos/data/repository/StreakRepository.kt`

**Purpose**: Remote streak management

**Integration Hook**:
- **Called by**: Refactored `StreakEngine.checkInDaily()`
- **Calls**: `FirebaseDataSource` or `SupabaseDataSource`

### 4.5 Content Gating Implementation

**New File**: `/app/src/main/java/com/focusblack/wallos/core/ContentGatekeeper.kt`

**Purpose**: Determine if user can access wallpaper based on streak

**Key Method**:
```kotlin
fun isWallpaperUnlocked(wall: Wall, userStreak: Int): Boolean {
    return when (wall.metadata.unlockRequirement.type) {
        "free" -> true
        "streak" -> userStreak >= wall.metadata.unlockRequirement.minStreak
        "premium" -> OwnershipStore.hasPro()
        else -> false
    }
}
```

**Integration Points**:
- **File**: `PackRegistry.kt`
- **Modification**: After line 32 in `listPacks()`
```kotlin
fun listPacks(context: Context): List<Pack> {
    val userStreak = StreakEngine.getStreak(context)
    return packs.values.map { pack ->
        pack.copy(walls = pack.walls.filter { wall ->
            ContentGatekeeper.isWallpaperUnlocked(wall, userStreak)
        })
    }
}
```

### 4.6 UI Integration Points

**File**: `/app/src/main/java/com/focusblack/wallos/ui/TodayFragment.kt`

**Modification Point**: Line 138, after `StreakEngine.onDailyApplied()`

**Add**:
```kotlin
// Sync streak with remote and check for newly unlocked content
lifecycleScope.launch {
    val userId = getUserId() // From Firebase Auth or device ID
    val checkInResult = StreakEngine.checkInDaily(requireContext(), userId)

    if (checkInResult.newlyUnlockedWallpapers.isNotEmpty()) {
        showUnlockNotification(checkInResult.newlyUnlockedWallpapers.size)
    }
}
```

**File**: `/app/src/main/java/com/focusblack/wallos/core/RotationWorker.kt`

**Modification Point**: Line 69, after `StreakEngine.onDailyApplied()`

**Add**:
```kotlin
// Background check-in
runBlocking {
    val userId = getUserId()
    StreakEngine.syncWithRemote(applicationContext, userId)
}
```

---

## Phase 5: User Authentication (Prerequisite for Remote Sync)

### 5.1 Authentication Strategy

**Approach**: Anonymous Authentication with optional account linking

**Why Anonymous First**:
- No user friction for streak tracking
- Maintains privacy-first design
- Can upgrade to full account later

### 5.2 Firebase Auth Integration

**Gradle Dependency** (`app/build.gradle` line 43):
```gradle
implementation 'com.google.firebase:firebase-auth-ktx'
```

**New File**: `/app/src/main/java/com/focusblack/wallos/auth/AuthManager.kt`

**Key Methods**:
```kotlin
suspend fun getOrCreateAnonymousUser(): String {
    // Sign in anonymously or return existing UID
}

suspend fun linkEmailPassword(email: String, password: String) {
    // Upgrade anonymous to full account
}
```

**Integration Point**:
- **File**: `MainActivity.kt`
- **Modification**: `onCreate()` method
```kotlin
lifecycleScope.launch {
    val userId = AuthManager.getOrCreateAnonymousUser()
    StreakEngine.syncWithRemote(this, userId)
}
```

### 5.3 Supabase Auth Integration

**Gradle Dependency** (`app/build.gradle` line 43):
```gradle
implementation 'io.github.jan-tennert.supabase:gotrue-kt:3.0.2'
```

**Integration**: Similar to Firebase, anonymous auth available

---

## Integration Hooks Summary Table

| Phase | File | Line(s) | Modification Type | Purpose |
|-------|------|---------|-------------------|---------|
| 1 | `model/Wall.kt` | 1-8 | **EXTEND** | Add metadata fields |
| 1 | `model/Pack.kt` | 3-9 | **EXTEND** | Add sync metadata |
| 1 | `core/PackRegistry.kt` | 8 | **WRAP** | Add persistence layer |
| 1 | `data/PackRepository.kt` | 14-22 | **REPLACE** | Use Firebase/Supabase SDK |
| 2 | `data/cache/WallpaperAssetCache.kt` | 19 | **ENHANCE** | Add priority queue |
| 2 | `ui/TodayFragment.kt` | 87-112 | **HOOK** | Use ImageCacheManager |
| 2 | `core/RotationScheduler.kt` | TBD | **ADD** | Schedule SyncWorker |
| 3 | **External Script** | N/A | **CREATE** | Content generation pipeline |
| 4 | `core/StreakEngine.kt` | 34 | **EXTEND** | Add remote sync methods |
| 4 | `core/PackRegistry.kt` | 32 | **FILTER** | Apply content gating |
| 4 | `ui/TodayFragment.kt` | 138 | **HOOK** | Sync streak on apply |
| 4 | `core/RotationWorker.kt` | 69 | **HOOK** | Sync streak on rotation |
| 5 | `ui/MainActivity.kt` | `onCreate()` | **INITIALIZE** | Auth + initial sync |

---

## New Files Required

### Core Android Files
1. `/app/src/main/java/com/focusblack/wallos/model/WallpaperModel.kt`
2. `/app/src/main/java/com/focusblack/wallos/data/repository/WallpaperRepository.kt`
3. `/app/src/main/java/com/focusblack/wallos/data/repository/StreakRepository.kt`
4. `/app/src/main/java/com/focusblack/wallos/data/remote/FirebaseDataSource.kt` (Option A)
5. `/app/src/main/java/com/focusblack/wallos/data/remote/SupabaseDataSource.kt` (Option B)
6. `/app/src/main/java/com/focusblack/wallos/data/cache/ImageCacheManager.kt`
7. `/app/src/main/java/com/focusblack/wallos/core/SyncWorker.kt`
8. `/app/src/main/java/com/focusblack/wallos/core/ContentGatekeeper.kt`
9. `/app/src/main/java/com/focusblack/wallos/auth/AuthManager.kt`

### External Infrastructure
1. `/automation/content_automator.py` (or `.js`)
2. `/automation/prompts/templates.json`
3. `/automation/config/generation_config.yaml`
4. `/automation/requirements.txt` (Python deps)

---

## Gradle Dependency Additions Summary

**File**: `/app/build.gradle`
**Location**: After line 64 (after Play Review dependency)

**Firebase Stack** (Option A):
```gradle
// Firebase
implementation platform('com.google.firebase:firebase-bom:33.7.0')
implementation 'com.google.firebase:firebase-firestore-ktx'
implementation 'com.google.firebase:firebase-storage-ktx'
implementation 'com.google.firebase:firebase-auth-ktx'
```

**Supabase Stack** (Option B):
```gradle
// Supabase
implementation 'io.github.jan-tennert.supabase:postgrest-kt:3.0.2'
implementation 'io.github.jan-tennert.supabase:storage-kt:3.0.2'
implementation 'io.github.jan-tennert.supabase:realtime-kt:3.0.2'
implementation 'io.github.jan-tennert.supabase:gotrue-kt:3.0.2'
implementation 'io.ktor:ktor-client-android:3.0.3'
```

**Additional**:
```gradle
// Room for local persistence (recommended)
implementation 'androidx.room:room-runtime:2.6.1'
implementation 'androidx.room:room-ktx:2.6.1'
kapt 'androidx.room:room-compiler:2.6.1'
```

---

## External Service Requirements

### Required Services
1. **Firebase Project** (Option A)
   - Firestore database
   - Cloud Storage bucket
   - Authentication
   - **Pricing**: Free tier → 50K reads/day, 20K writes/day

2. **Supabase Project** (Option B)
   - PostgreSQL database
   - Storage bucket
   - Auth service
   - **Pricing**: Free tier → 500MB database, 1GB storage

3. **Stability AI Account**
   - API key for SDXL/SD3
   - **Pricing**: ~$0.004 per image (512x512), ~$0.02 for high-res

4. **Cloud Hosting** (for automation script)
   - Option A: Cloud Functions (Firebase) - Serverless
   - Option B: Cloud Run (GCP) - Containerized
   - Option C: VPS (DigitalOcean/Railway) - Traditional
   - **Recommended**: Railway.app ($5/month, includes cron)

### Optional Services
1. **Real-ESRGAN** (self-hosted upscaling) - Free, GPU required
2. **CDN** (Cloudflare/Fastly) - For image delivery optimization
3. **Monitoring** (Sentry) - Error tracking for automation script

---

## Implementation Order & Dependencies

```
Phase 1: Data Layer Decoupling (2-3 days)
  ↓ (Enables remote data)
Phase 2: Dynamic Sync Engine (3-4 days)
  ↓ (Enables content updates)
Phase 5: User Authentication (1-2 days)
  ↓ (Enables user-specific data)
Phase 4: Streak-Gating Logic (2-3 days)
  ↓ (Enables content unlocking)
Phase 3: Autonomous Content Pipeline (4-5 days, parallel track)
  ↓ (Generates content)
Integration & Testing (2-3 days)
```

**Total Estimated Timeline**: 14-18 days of development

---

## Risk Mitigation & Backward Compatibility

### Backward Compatibility Strategy
1. **Dual Mode**: App must work offline with local drawables
2. **Graceful Degradation**: If remote fetch fails, use local assets
3. **Progressive Enhancement**: New features don't break existing flows

### Risk Mitigation
1. **Remote Unavailability**: Local cache + fallback to static assets
2. **API Rate Limits**: Exponential backoff + retry logic
3. **Large Download Sizes**: WiFi-only constraint + progressive loading
4. **User Privacy**: Anonymous auth + opt-in for account upgrade
5. **Generation Failures**: Queue + retry + manual review system

---

## Decision Points Requiring Verification

Before proceeding with implementation, please verify:

1. **Backend Choice**: Firebase or Supabase?
   - Firebase: Better Android integration, more mature
   - Supabase: Open source, PostgreSQL, better pricing at scale

2. **Generation Frequency**: How often to generate new wallpapers?
   - Daily? Weekly? On-demand?

3. **Unlock Mechanism**: Pure streak-based or hybrid?
   - Only streak (current proposal)
   - Streak + IAP for instant unlock
   - Time-based release (all users unlock after N days)

4. **Automation Hosting**: Where to run content_automator?
   - Cloud Functions (event-driven)
   - Scheduled task (cron)
   - Manual trigger (admin dashboard)

5. **Authentication Strategy**: Anonymous vs. required sign-in?
   - Anonymous first (current proposal)
   - Email required (better data persistence)

---

## Next Steps

**AWAITING ARCHITECTURAL VERIFICATION**

Please review this System Design Map and approve:
1. ✓ Backend technology choice (Firebase vs. Supabase)
2. ✓ Integration hook locations
3. ✓ New file structure
4. ✓ External service requirements
5. ✓ Implementation order

Once verified, I will proceed with:
1. Phase 1 implementation (Model extensions + Repository pattern)
2. Gradle dependency updates
3. Data source integration
4. Testing framework setup

---

## Appendix: Code Impact Analysis

### Files Modified (Existing)
- `model/Wall.kt` - Add metadata fields
- `model/Pack.kt` - Add sync fields
- `core/PackRegistry.kt` - Add repository layer
- `data/PackRepository.kt` - Replace with SDK calls
- `data/cache/WallpaperAssetCache.kt` - Add priority queue
- `core/StreakEngine.kt` - Add remote sync
- `ui/TodayFragment.kt` - Add remote check-in
- `core/RotationWorker.kt` - Add remote sync
- `ui/MainActivity.kt` - Add auth initialization
- `app/build.gradle` - Add dependencies

**Total Lines Modified (Estimated)**: ~300 lines across 10 files

### Files Created (New)
- 9 new Android Kotlin files
- 3-4 automation script files
- Database migration scripts

**Total Lines Added (Estimated)**: ~2000 lines

### Test Coverage Required
- Unit tests for repositories
- Integration tests for sync logic
- UI tests for gating
- End-to-end tests for full pipeline

---

**Document End** - Awaiting verification before code generation phase.
