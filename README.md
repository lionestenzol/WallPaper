# focus-black

Daily AMOLED ritual wallpaper app with automatic rotation, streak tracking, and minimalist black designs.

## Overview

**focus-black** is an Android wallpaper app that automatically rotates through a collection of AMOLED-optimized minimal wallpapers. Built with Kotlin, Material 3, and WorkManager for reliable daily rotation.

- **Package**: `com.focusblack.wallos`
- **Min SDK**: 26 (Android 8.0+)
- **Target SDK**: 36 (Android 15)

## Features

### Implemented
- **Automatic Daily Rotation** - WorkManager cycles through wallpapers every 24 hours
- **Manual Apply** - Tap to instantly apply the current wallpaper
- **Manual Rotation Trigger** - Settings button to trigger immediate rotation
- **Apply Status Tracking** - Track last wallpaper apply result with timestamp and error details
- **Wallpaper Apply Target** - Choose to apply wallpaper to home screen, lock screen, or both
- **Battery Optimization** - Delay rotation when battery is low (optional)
- **Streak Tracking** - Tracks consecutive days of wallpaper application
- **7 AMOLED Wallpapers** - Pure black backgrounds for power savings
- **Functional Home Widgets** - 4 working widgets (FocusRing, ModeSigil, DateGlyph, BatteryHalo)
- **Remote Pack Loading** - Download wallpaper packs from server
- **Device-Adaptive Scaling** - Smart center-crop for any screen size
- **Structured Logging** - Analytics-ready event logging with WallosLogger
- **Apply Status UI** - Visual feedback showing "Applying...", "Applied", or "Failed" with retry action
- **Review Gate** - In-app review prompt after 3 applies
- **Bottom Navigation** - Today / Packs / Widgets tabs
- **Material 3 Dark UI** - Modern design with pure black theme
- **Pro Unlock** - Single IAP unlocks all content
- **Settings Screen** - Auto-rotate toggle, restore purchases, pro status

### Planned
- Additional wallpaper packs
- Full-screen wallpaper preview

## Genesis Pack Wallpapers

All wallpapers feature **pure black (#000000)** backgrounds optimized for AMOLED displays:

| # | Name | Description |
|---|------|-------------|
| 1 | Genesis Prime | Minimal white dot |
| 2 | Genesis Seal | White triangle symbol |
| 3 | Horizon | Thin horizontal line |
| 4 | Circle | Large centered white circle |
| 5 | Crescent | Moon-like crescent shape |
| 6 | Grid | Subtle gray grid pattern |
| 7 | Accent Line | Deep red vertical accent |

## Getting Started

### Prerequisites
- Android Studio Hedgehog or newer
- JDK 17
- Android device or emulator (API 26+)

### Build & Run

```bash
# Clone the repository
git clone <repo-url>
cd focus-black

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

Or open in Android Studio and click Run.

### Usage

1. Launch **focus-black** from app drawer
2. Navigate to **Today** tab
3. Tap **"Apply Wallpaper"** button
4. Wallpaper changes immediately
5. Auto-rotation continues in background

## Project Structure

```
app/src/main/
├── java/com/focusblack/wallos/
│   ├── billing/           # Play Billing integration
│   │   ├── BillingManager.kt
│   │   ├── BillingRepository.kt
│   │   └── SkuCatalog.kt
│   ├── core/              # Core business logic
│   │   ├── PackRegistry.kt
│   │   ├── RotationScheduler.kt
│   │   ├── RotationWorker.kt
│   │   ├── StreakEngine.kt
│   │   ├── WallpaperEngine.kt
│   │   ├── WallosLogger.kt
│   │   └── WallosAnalytics.kt
│   ├── data/              # Data layer
│   │   ├── cache/
│   │   │   ├── WallpaperAssetCache.kt
│   │   │   └── DiskLruCache.kt
│   │   ├── OwnershipStore.kt
│   │   ├── PackRepository.kt
│   │   └── ReviewGate.kt
│   ├── model/             # Data classes
│   │   ├── Pack.kt
│   │   └── Wall.kt
│   ├── ui/                # UI components
│   │   ├── MainActivity.kt
│   │   ├── TodayFragment.kt
│   │   ├── PacksFragment.kt
│   │   ├── PackDetailActivity.kt
│   │   ├── PackAdapter.kt
│   │   ├── WallpaperAdapter.kt
│   │   ├── WidgetsFragment.kt
│   │   ├── SettingsActivity.kt
│   │   └── SettingsFragment.kt
│   ├── util/
│   │   └── ReviewHelper.kt
│   └── widget/            # Home screen widgets
│       ├── BaseWallpaperWidget.kt
│       ├── BatteryHaloWidget.kt
│       ├── DateGlyphWidget.kt
│       ├── FocusRingWidget.kt
│       ├── ModeSigilWidget.kt
│       ├── WidgetUpdater.kt
│       ├── WidgetDataSource.kt
│       ├── WidgetState.kt
│       └── WidgetViews.kt
└── res/
    ├── drawable/          # Wallpapers and icons
    ├── layout/            # UI layouts
    ├── menu/              # Navigation menus
    ├── values/            # Colors, strings, themes
    └── xml/               # Preferences
```

## Architecture

### Layers

| Layer | Purpose | Components |
|-------|---------|------------|
| **UI** | User interface | Activities, Fragments, Adapters |
| **Core** | Business logic | WallpaperEngine, RotationScheduler, StreakEngine |
| **Data** | Persistence | OwnershipStore, ReviewGate (SharedPreferences) |
| **Billing** | Purchases | BillingManager, BillingRepository |

### Key Components

#### WallpaperEngine
Applies wallpapers to the device. Supports both local drawables and remote URLs. Uses device-adaptive scaling with center-crop strategy to fit any screen size. Returns `ApplyResult` with success status and error details. Supports applying to home screen, lock screen, or both (Android N+). Converts vector drawables to bitmaps via Canvas and sets them through WallpaperManager.

#### RotationScheduler / RotationWorker
Uses WorkManager to schedule daily wallpaper rotation. RotationWorker executes in the background to cycle through wallpapers.

#### StreakEngine
Tracks consecutive days of wallpaper application. Resets if a day is missed, increments on daily use.

#### BillingManager
Handles Google Play Billing Library integration:
- Product details queries
- Purchase flow
- Purchase acknowledgment
- Purchase restoration

#### PackRegistry
Singleton registry of available wallpaper packs. Currently contains Genesis pack with 7 wallpapers. Supports loading remote packs via `loadRemotePacks()`.

#### WidgetUpdater
Updates all home screen widgets when wallpaper changes. Ensures widgets always display current wallpaper information.

#### PackRepository
Fetches remote wallpaper pack metadata from server. Enables dynamic pack loading without app updates.

#### WallpaperAssetCache
Downloads and caches remote wallpaper images using DiskLruCache. Provides fast access to previously downloaded wallpapers.

#### WallosLogger
Structured logging system for consistent event tracking. Formats logs as key-value pairs (e.g., `event=wallpaper_apply_started wall_id=genesis_prime`). Provides foundation for analytics integration with Firebase, Mixpanel, or similar services.

#### WallosAnalytics
Analytics hooks that track user actions and app events. Currently outputs to logcat, ready for integration with analytics platforms.

## Configuration

### Adding Wallpapers

1. Add vector drawable to `res/drawable/wall_name.xml`
2. Register in `PackRegistry.kt`:

```kotlin
Wall(
    id = "wall_name",
    title = "Display Name",
    drawableName = "wall_name"
)
```

### Play Store Setup

1. Create app in Google Play Console
2. Create in-app product:
   - Product ID: `pro_unlock`
   - Type: One-time purchase
3. Upload to internal testing track
4. Add license testers

## Permissions

| Permission | Purpose |
|------------|---------|
| `SET_WALLPAPER` | Apply wallpapers to device |
| `WAKE_LOCK` | WorkManager background tasks |
| `BILLING` | Google Play purchases |

## Dependencies

```gradle
// Kotlin
kotlin-stdlib:2.0.21
kotlinx-coroutines-android:1.8.1

// AndroidX
core-ktx:1.12.0
appcompat:1.7.1
fragment-ktx:1.8.9
lifecycle-runtime-ktx:2.10.0
preference-ktx:1.2.1
work-runtime-ktx:2.9.0
recyclerview:1.4.0
constraintlayout:2.2.1

// UI
material:1.13.0

// Billing
billing-ktx:7.1.1
```

## Development

### Testing Rotation

For faster testing, modify `RotationScheduler.kt`:

```kotlin
// Change from 1 day to 15 minutes
PeriodicWorkRequestBuilder<RotationWorker>(15, TimeUnit.MINUTES)
```

### Debug Builds

Debug builds have:
- ProGuard disabled
- Full logging enabled

### Release Builds

Release builds include:
- ProGuard minification
- Resource shrinking

## Known Limitations

1. ReviewHelper is a placeholder (no actual Play review integration)
2. Single pack included by default (Genesis) - remote packs require server URL

## Version History

| Version | Changes |
|---------|---------|
| 1.0.0 | Initial release with core features |

## License

All rights reserved.
