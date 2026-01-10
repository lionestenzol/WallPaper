# focus-black

Daily AMOLED ritual wallpaper app with automatic rotation, streak tracking, and minimalist black designs.

## 🎯 Overview

**focus-black** is a functional Android wallpaper app that automatically rotates through a collection of AMOLED-optimized minimal wallpapers. Built with Kotlin, Material3, and WorkManager for reliable daily rotation.

- **Package**: `com.focusblack.wallos`
- **Min SDK**: 26 (Android 8.0+)
- **Target SDK**: 34 (Android 14)

## ✨ Features

### Currently Implemented
- ✅ **Automatic Daily Rotation** - WorkManager cycles through wallpapers every 24 hours
- ✅ **Manual Apply** - Tap to instantly apply the next wallpaper in rotation
- ✅ **Streak Tracking** - Tracks consecutive days of wallpaper applies
- ✅ **7 AMOLED Wallpapers** - Pure black backgrounds for power savings
- ✅ **Review Gate** - In-app review prompt after 3 applies
- ✅ **Bottom Navigation** - Today / Packs / Widgets tabs
- ✅ **Material3 UI** - Modern design with dark theme

### Planned Features
- 🔜 Serialized premium packs with Play Billing
- 🔜 Home screen widgets (Focus Ring, Mode Sigil, Date Glyph, Battery Halo)
- 🔜 Live wallpaper support
- 🔜 Additional wallpaper packs

**Play Billing SKUs**: `pro_unlock`, `pack_genesis_001`

## 🎨 Genesis Pack Wallpapers

All wallpapers feature **pure black (#000000)** backgrounds optimized for AMOLED displays:

1. **Genesis Prime** - Minimal white dot
2. **Genesis Seal** - White triangle symbol
3. **Horizon** - Thin horizontal line
4. **Circle** - Large centered white circle
5. **Crescent** - Moon-like crescent shape
6. **Grid** - Subtle gray grid pattern
7. **Accent Line** - Deep red vertical accent

### Preview Gallery
Open `wallpaper-gallery.html` in a browser to preview all 7 wallpapers with interactive full-screen views.

## 🚀 Getting Started

### Build & Install

1. **Open in Android Studio**
   ```bash
   # Clone or open the project
   cd focus-black
   # Android Studio will auto-sync Gradle
   ```

2. **Connect Device or Emulator**
   - Physical device: Enable USB debugging
   - Emulator: Start any Android 8.0+ virtual device

3. **Run**
   - Click Run button (▶️) or press `Shift + F10`
   - App will install and launch automatically

### Usage

1. Launch **focus-black** from your app drawer
2. Navigate to **Today** tab
3. Tap **"Apply Today's Wallpaper"** button
4. Wallpaper changes immediately
5. Each tap cycles to the next wallpaper (1→2→3...→7→1)
6. Check your streak counter to track daily usage

### Automatic Rotation
- WorkManager schedules daily rotation at 24-hour intervals
- Runs in background even when app is closed
- Continues sequence from last applied wallpaper

## 📱 UI Preview

Open `ui-preview.html` in a browser to see an interactive phone mockup with working navigation and buttons.

## 🛠️ Technical Details

### Architecture
- **MVVM pattern** with repository layer
- **WorkManager** for reliable background rotation
- **SharedPreferences** for state persistence
- **Material3** components and theming

### Key Components

#### Core
- `WallpaperEngine` - Applies wallpapers via WallpaperManager API
- `RotationScheduler` - Schedules periodic WorkManager tasks
- `RotationWorker` - Background worker for daily rotation
- `StreakEngine` - Tracks consecutive daily applies
- `PackRegistry` - Centralized wallpaper catalog

#### Data
- `UserRepository` - User state management
- `OwnershipStore` - Purchase ownership tracking
- `ReviewGate` - In-app review triggering logic

#### UI
- `MainActivity` - Host with bottom navigation
- `TodayFragment` - Manual apply and streak display
- `PacksFragment` - Browse available packs
- `WidgetsFragment` - Widget configuration (stub)

### Dependencies
```gradle
- Kotlin 1.9.10
- AndroidX Core, AppCompat, Fragment
- Material Components 1.9.0
- WorkManager 2.8.1
- Play Billing 6.0.1
- Preference 1.2.0
```

## 📂 Project Structure

```
app/src/main/
├── AndroidManifest.xml
├── java/com/focusblack/wallos/
│   ├── core/              # Core wallpaper logic
│   ├── billing/           # Play Billing (stub)
│   ├── data/              # Repositories and storage
│   ├── model/             # Data models
│   ├── ui/                # Activities and fragments
│   ├── widget/            # Widget providers (stubs)
│   └── util/              # Helpers
└── res/
    ├── drawable/          # Wallpaper vector graphics
    ├── layout/            # UI layouts
    ├── menu/              # Navigation menu
    └── values/            # Strings, colors, themes
```

## 🔐 Permissions

- `SET_WALLPAPER` - Required to change system wallpaper
- `WAKE_LOCK` - Used by WorkManager for background tasks

## 🧪 Development

### Adding New Wallpapers

1. Create vector drawable in `res/drawable/`
   - Use 1080×2340 viewport
   - Start with pure black background
   - Add minimal white/colored elements

2. Register in `PackRegistry.kt`
   ```kotlin
   Wall(id = "new_wall", title = "New Wall", drawableName = "new_wall")
   ```

3. Rebuild and run

### Testing Rotation
- Change WorkManager interval in `RotationScheduler.kt`
- Use `PeriodicWorkRequestBuilder<RotationWorker>(15, TimeUnit.MINUTES)` for testing
- Remember to revert to 1 day for production

## 📄 License

MIT License - See LICENSE file for details

## 🤝 Contributing

This is a personal project scaffold. Feel free to fork and customize for your own use.

---

**Version**: 0.1.0
**Status**: Functional prototype with real wallpaper rotation
**Platform**: Android 8.0+ (API 26+)
