# focus-black LLM Context Guide

This document provides context for AI/LLM assistants working on the focus-black Android codebase.

## Project Identity

- **App Name:** focus-black (package: com.focusblack.wallos)
- **Type:** Android wallpaper app with AMOLED-optimized designs
- **Language:** Kotlin
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 34 (Android 14)

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                        UI Layer                              │
│  MainActivity → TodayFragment, GalleryFragment, SettingsFragment │
│  PackDetailActivity                                          │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     Business Logic                           │
│  WallpaperEngine │ PackRegistry │ StreakEngine │ RotationScheduler │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     Data/Billing Layer                       │
│  BillingManager │ BillingRepository │ OwnershipStore        │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     Persistence                              │
│  SharedPreferences (focus_black_prefs)                      │
└─────────────────────────────────────────────────────────────┘
```

## Directory Structure

```
app/src/main/
├── java/com/focusblack/wallos/
│   ├── billing/          # Play Billing integration
│   │   ├── BillingManager.kt      # Low-level billing API
│   │   ├── BillingRepository.kt   # Singleton billing wrapper
│   │   └── OwnershipStore.kt      # Purchase state persistence
│   ├── data/             # Data layer
│   │   └── PrefsKeys.kt           # SharedPreferences key constants
│   ├── model/            # Data classes
│   │   ├── Wallpaper.kt           # Wallpaper data model
│   │   └── WallpaperPack.kt       # Pack data model
│   ├── ui/               # UI components
│   │   ├── MainActivity.kt        # Main activity with bottom nav
│   │   ├── PackDetailActivity.kt  # Pack viewer/purchase screen
│   │   ├── SettingsActivity.kt    # Settings host
│   │   ├── TodayFragment.kt       # Today's wallpaper display
│   │   ├── GalleryFragment.kt     # Pack grid browser
│   │   ├── SettingsFragment.kt    # Preference screen
│   │   ├── PackAdapter.kt         # RecyclerView adapter for packs
│   │   └── WallpaperAdapter.kt    # RecyclerView adapter for wallpapers
│   ├── util/             # Utilities
│   │   ├── ReviewGate.kt          # Review prompt timing logic
│   │   └── ReviewHelper.kt        # Play Review API (stub)
│   ├── wallpaper/        # Core wallpaper logic
│   │   ├── WallpaperEngine.kt     # Wallpaper application
│   │   ├── PackRegistry.kt        # Pack definitions
│   │   └── RotationScheduler.kt   # WorkManager scheduling
│   ├── widget/           # Home screen widgets (stubs)
│   │   ├── StreakWidget.kt
│   │   ├── TodayWidget.kt
│   │   └── QuickApplyWidget.kt
│   └── worker/           # Background work
│       └── DailyRotationWorker.kt # Daily wallpaper rotation
└── res/
    ├── drawable/         # Vector drawables, icons
    ├── drawable-nodpi/   # Wallpaper images (genesis_*.webp)
    ├── layout/           # XML layouts
    ├── values/           # Colors, strings, themes
    └── xml/              # Preferences, widget providers
```

## Key Classes Reference

### WallpaperEngine
**Location:** `wallpaper/WallpaperEngine.kt`
**Purpose:** Applies wallpapers to device
**Key Method:** `applyWallpaper(context, resourceId): Boolean`
**Note:** Uses hardcoded 1080x2340 dimensions - may need dynamic sizing

### PackRegistry
**Location:** `wallpaper/PackRegistry.kt`
**Purpose:** Defines available wallpaper packs
**Key Method:** `getAllPacks(): List<WallpaperPack>`
**Current State:** Only Genesis Pack defined (7 wallpapers)

### StreakEngine
**Location:** `wallpaper/StreakEngine.kt`
**Purpose:** Tracks daily wallpaper usage streaks
**Key Methods:**
- `recordToday()` - Mark today as active
- `getCurrentStreak(): Int` - Get current streak count
- `getLongestStreak(): Int` - Get all-time best

### BillingManager
**Location:** `billing/BillingManager.kt`
**Purpose:** Play Billing Library integration
**Product ID:** `pro_unlock` (one-time purchase)
**Key Methods:**
- `queryPurchases()` - Check existing purchases
- `launchPurchaseFlow(activity)` - Start purchase
**Dependencies:** billing-ktx:7.1.1

### BillingRepository
**Location:** `billing/BillingRepository.kt`
**Purpose:** Singleton wrapper for BillingManager
**Pattern:** `BillingRepository.getInstance(context)`
**Note:** Provides app-wide billing access

### OwnershipStore
**Location:** `billing/OwnershipStore.kt`
**Purpose:** Persists Pro unlock state locally
**Key Methods:**
- `isPro(): Boolean` - Check unlock status
- `setPro(unlocked: Boolean)` - Update status

### RotationScheduler
**Location:** `wallpaper/RotationScheduler.kt`
**Purpose:** Schedules daily wallpaper rotation
**Implementation:** WorkManager with daily periodic work
**Worker:** `DailyRotationWorker`

## Data Models

### Wallpaper
```kotlin
data class Wallpaper(
    val id: String,           // e.g., "genesis_001"
    val resourceId: Int,      // R.drawable.genesis_001
    val name: String,         // Display name
    val isPremium: Boolean    // Requires Pro unlock
)
```

### WallpaperPack
```kotlin
data class WallpaperPack(
    val id: String,           // e.g., "GENESIS_001"
    val name: String,         // "Genesis Pack"
    val description: String,
    val wallpapers: List<Wallpaper>,
    val isPremium: Boolean,
    val previewResourceId: Int
)
```

## SharedPreferences Keys

**File:** `focus_black_prefs` (Context.MODE_PRIVATE)

| Key | Type | Purpose |
|-----|------|---------|
| `pref_auto_rotate` | Boolean | Enable daily rotation |
| `pref_rotate_time` | String | Rotation time (HH:mm) |
| `pref_apply_lock_screen` | Boolean | Apply to lock screen |
| `pref_apply_home_screen` | Boolean | Apply to home screen |
| `current_wallpaper_index` | Int | Current wallpaper position |
| `streak_current` | Int | Current streak count |
| `streak_longest` | Int | All-time best streak |
| `streak_last_date` | String | Last active date (yyyy-MM-dd) |
| `pro_unlocked` | Boolean | Pro purchase status |
| `wallpaper_apply_count` | Int | Total applications |
| `review_prompt_shown` | Boolean | Review prompt displayed |

## Common Patterns

### Getting Context-Dependent Singletons
```kotlin
val billing = BillingRepository.getInstance(context)
val prefs = context.getSharedPreferences("focus_black_prefs", Context.MODE_PRIVATE)
```

### Applying a Wallpaper
```kotlin
val engine = WallpaperEngine()
val success = engine.applyWallpaper(context, R.drawable.genesis_001)
if (success) {
    StreakEngine(context).recordToday()
}
```

### Checking Pro Status
```kotlin
val isPro = OwnershipStore(context).isPro()
// OR
val isPro = BillingRepository.getInstance(context).isPro.value
```

### Scheduling Rotation
```kotlin
RotationScheduler.schedule(context, hour = 8, minute = 0)
// To cancel:
RotationScheduler.cancel(context)
```

## Known Issues & Technical Debt

### Critical
1. **Widgets not registered** - Widget classes exist but not declared in AndroidManifest.xml

### High Priority
2. **ReviewHelper is a stub** - Just logs, needs Play Core Review API integration
3. **Hardcoded dimensions** - WallpaperEngine uses 1080x2340, should be dynamic
4. **Hardcoded pack ID** - "GENESIS_001" appears in multiple files

### Medium Priority
5. **Unused isPro parameter** - PackAdapter constructor takes isPro but doesn't use it
6. **No network error handling** - BillingManager lacks retry logic
7. **Singleton memory** - BillingRepository holds Context reference

### Low Priority
8. **Unused string resources** - Several strings defined but not used
9. **Dead layout** - activity_main.xml exists but MainActivity uses fragment-based layout

## Build Configuration

### Dependencies (build.gradle)
```groovy
// Core
implementation 'androidx.core:core-ktx:1.12.0'
implementation 'androidx.appcompat:appcompat:1.6.1'
implementation 'com.google.android.material:material:1.11.0'

// Billing
implementation 'com.android.billingclient:billing-ktx:7.1.1'

// WorkManager
implementation 'androidx.work:work-runtime-ktx:2.9.0'

// Preferences
implementation 'androidx.preference:preference-ktx:1.2.1'
```

### Signing
- Keystore: `focus-black.keystore` (not in repo)
- Key alias: `focus-black`
- See docs/PLAY_STORE_SETUP.md for signing configuration

## Testing Notes

### Manual Testing Checklist
- [ ] Wallpaper applies to home screen
- [ ] Wallpaper applies to lock screen
- [ ] Streak increments on first daily apply
- [ ] Streak resets after missing a day
- [ ] Pro purchase flow completes
- [ ] Pro status persists across app restart
- [ ] Daily rotation triggers at scheduled time
- [ ] Settings preferences persist

### License Testing
Add test accounts to Play Console License Testing to test purchases without charges.

## File Naming Conventions

- **Kotlin files:** PascalCase (e.g., `WallpaperEngine.kt`)
- **Layouts:** snake_case with prefix (e.g., `fragment_today.xml`, `item_pack.xml`)
- **Drawables:** snake_case (e.g., `genesis_001.webp`, `ic_apply.xml`)
- **Resources:** snake_case (e.g., `@string/app_name`, `@color/black`)

## Important Constraints

1. **Do not update dependencies** without explicit request - version changes can break the build
2. **Test billing on signed release builds** - debug builds won't work with Play Billing
3. **Wallpapers must be in drawable-nodpi** - prevents unwanted scaling
4. **Keep pure black (#000000)** - required for AMOLED battery savings
5. **Min SDK 26** - uses modern APIs, no legacy support needed

## Quick Reference Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release AAB
./gradlew bundleRelease

# Run lint checks
./gradlew lint

# Clean build
./gradlew clean

# Install debug build
./gradlew installDebug
```

## Related Documentation

- `README.md` - Project overview and setup
- `docs/API.md` - Detailed API documentation
- `docs/PLAY_STORE_SETUP.md` - Publishing guide
