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
- **Streak Tracking** - Tracks consecutive days of wallpaper application
- **7 AMOLED Wallpapers** - Pure black backgrounds for power savings
- **Review Gate** - In-app review prompt after 3 applies
- **Bottom Navigation** - Today / Packs / Widgets tabs
- **Material 3 Dark UI** - Modern design with pure black theme
- **Pro Unlock** - Single IAP unlocks all content
- **Settings Screen** - Auto-rotate toggle, restore purchases, pro status

### Planned
- Functional home screen widgets
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
│   │   └── WallpaperEngine.kt
│   ├── data/              # Local persistence
│   │   ├── OwnershipStore.kt
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
│   └── widget/            # Widget stubs
│       ├── BatteryHaloWidget.kt
│       ├── DateGlyphWidget.kt
│       ├── FocusRingWidget.kt
│       └── ModeSigilWidget.kt
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
Applies wallpapers to the device by converting vector drawables to bitmaps via Canvas and setting them through WallpaperManager.

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
Singleton registry of available wallpaper packs. Currently contains Genesis pack with 7 wallpapers.

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

1. Widgets are stub implementations (not functional)
2. ReviewHelper is a placeholder (no actual Play review integration)
3. Wallpaper dimensions are hardcoded (1080x2340)
4. Single pack only (Genesis)

## Version History

| Version | Changes |
|---------|---------|
| 1.0.0 | Initial release with core features |

## License

All rights reserved.
