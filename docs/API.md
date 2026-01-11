# API Documentation

Technical documentation for focus-black internal APIs.

## Core Package

### WallpaperEngine

Singleton object that handles wallpaper application.

```kotlin
object WallpaperEngine {
    fun applyWall(context: Context, wall: Wall)
}
```

**Parameters:**
- `context` - Android context
- `wall` - Wall object containing drawable reference

**Behavior:**
1. Looks up drawable resource by name
2. Converts vector drawable to bitmap (1080x2340)
3. Sets as system wallpaper via WallpaperManager

**Notes:**
- Catches and logs exceptions internally
- Does not return success/failure status

---

### PackRegistry

Singleton registry of available wallpaper packs.

```kotlin
object PackRegistry {
    fun getPack(id: String): Pack?
    fun listPacks(): List<Pack>
}
```

**getPack(id)**
- Returns Pack with matching ID or null
- Current IDs: `"GENESIS_001"`

**listPacks()**
- Returns all registered packs

---

### RotationScheduler

Manages WorkManager scheduling for daily rotation.

```kotlin
object RotationScheduler {
    fun scheduleDailyRotation(context: Context)
    fun cancel(context: Context)
}
```

**scheduleDailyRotation(context)**
- Schedules RotationWorker to run every 24 hours
- Uses `ExistingPeriodicWorkPolicy.KEEP` (won't replace existing)

**cancel(context)**
- Cancels scheduled rotation work

---

### StreakEngine

Tracks consecutive days of wallpaper application.

```kotlin
object StreakEngine {
    fun onDailyApplied(context: Context)
    fun getStreak(context: Context): Int
}
```

**onDailyApplied(context)**
- Call when user applies wallpaper
- Increments streak if consecutive day
- Resets to 1 if gap detected
- Same-day calls don't increment

**getStreak(context)**
- Returns current streak count (0 if never applied)

---

## Billing Package

### BillingManager

Low-level Play Billing client wrapper.

```kotlin
class BillingManager(context: Context) : PurchasesUpdatedListener {
    var onPurchaseComplete: ((success: Boolean) -> Unit)?

    fun startConnection(onReady: (() -> Unit)?, onError: (() -> Unit)?)
    suspend fun queryProductDetails(): ProductDetails?
    fun getProPrice(): String?
    fun launchPurchaseFlow(activity: Activity): Boolean
    suspend fun queryPurchases(): Boolean
    fun isPro(): Boolean
    fun endConnection()
}
```

**startConnection(onReady, onError)**
- Establishes connection to Play Billing
- Callbacks invoked on main thread

**queryProductDetails()**
- Fetches product info from Play Store
- Must be called before launchPurchaseFlow
- Returns ProductDetails or null

**getProPrice()**
- Returns formatted price string (e.g., "$2.99")
- Returns null if product details not loaded

**launchPurchaseFlow(activity)**
- Opens Play Store purchase dialog
- Returns false if product details not loaded
- Result delivered via onPurchaseComplete callback

**queryPurchases()**
- Checks for existing purchases
- Updates OwnershipStore if Pro found
- Returns true if Pro purchase exists

**isPro()**
- Local check via OwnershipStore

---

### BillingRepository

High-level billing API for UI consumption.

```kotlin
class BillingRepository private constructor(application: Application) {
    companion object {
        fun getInstance(application: Application): BillingRepository
    }

    suspend fun initialize(): Boolean
    suspend fun loadProPrice(): String?
    fun isPro(): Boolean
    fun purchasePro(activity: Activity, onComplete: (success: Boolean) -> Unit)
    suspend fun restorePurchases(): Boolean
    fun getCachedProPrice(): String?
    fun destroy()
}
```

**getInstance(application)**
- Returns singleton instance
- Thread-safe initialization

**initialize()**
- Establishes billing connection
- Returns true on success, false on failure
- Safe to call multiple times

**loadProPrice()**
- Loads and returns Pro price
- Initializes if needed

**isPro()**
- Synchronous local check

**purchasePro(activity, onComplete)**
- Launches purchase flow
- Callback with success/failure result

**restorePurchases()**
- Queries Play Store for existing purchases
- Updates local ownership state
- Returns true if Pro found

---

### SkuCatalog

Product ID constants.

```kotlin
object SkuCatalog {
    const val SKU_PRO = "pro_unlock"
}
```

---

## Data Package

### OwnershipStore

Manages Pro ownership state in SharedPreferences.

```kotlin
class OwnershipStore(context: Context) {
    fun isPro(): Boolean
    fun setPro(owned: Boolean)
}
```

**Storage Key:** `own_pro_unlock`

---

### ReviewGate

Controls in-app review prompt timing.

```kotlin
class ReviewGate(context: Context) {
    fun recordApply()
    fun shouldShowReview(): Boolean
    fun setShown()
}
```

**recordApply()**
- Increments apply counter

**shouldShowReview()**
- Returns true if: applies >= 3 AND not already shown

**setShown()**
- Marks review as shown (won't trigger again)

---

## Model Package

### Pack

```kotlin
data class Pack(
    val id: String,        // Unique identifier (e.g., "GENESIS_001")
    val title: String,     // Display name (e.g., "Genesis Pack")
    val sku: String,       // Play Store SKU (unused currently)
    val walls: List<Wall>  // Wallpapers in this pack
)
```

### Wall

```kotlin
data class Wall(
    val id: String,          // Unique identifier
    val title: String,       // Display name
    val drawableName: String // Resource name (without R.drawable prefix)
)
```

---

## UI Package

### MainActivity

Main app entry point with bottom navigation.

**Navigation Destinations:**
- `R.id.nav_today` → TodayFragment
- `R.id.nav_packs` → PacksFragment
- `R.id.nav_widgets` → WidgetsFragment

**Menu:**
- `R.id.action_settings` → SettingsActivity

---

### TodayFragment

Today's wallpaper preview and apply functionality.

**Views:**
- `iv_preview` - Wallpaper preview image
- `tv_wall_title` - Current wallpaper name
- `tv_pack_info` - Pack name and position (e.g., "Genesis Pack - 1/7")
- `tv_streak` - Current streak count
- `btn_apply` - Apply wallpaper button
- `progress_apply` - Loading indicator

**Behavior:**
- Displays current wallpaper in rotation
- Apply button sets wallpaper and advances to next
- Updates streak on apply
- Triggers review prompt via ReviewGate

---

### PacksFragment

Browse available wallpaper packs.

**Views:**
- `rv_packs` - RecyclerView with PackAdapter

**Behavior:**
- Lists all packs from PackRegistry
- Clicking pack opens PackDetailActivity

---

### PackDetailActivity

View wallpapers within a pack.

**Intent Extras:**
- `EXTRA_PACK_ID` (String) - Pack ID to display

**Views:**
- `toolbar` - Back navigation
- `tv_pack_title` - Pack name
- `tv_pack_count` - Wallpaper count
- `rv_wallpapers` - Grid of wallpapers (2 columns)

---

### SettingsFragment

App settings and account management.

**Preferences:**
| Key | Type | Description |
|-----|------|-------------|
| `auto_rotate` | SwitchPreference | Enable/disable daily rotation |
| `pro_status` | Preference | Shows Free/Pro status |
| `restore_purchases` | Preference | Restore Play Store purchases |
| `unlock_pro` | Preference | Purchase Pro (hidden if already Pro) |
| `version` | Preference | App version display |

---

## Util Package

### ReviewHelper

```kotlin
object ReviewHelper {
    fun showReviewIfAppropriate(activity: Activity)
}
```

**Note:** Currently a stub - logs but doesn't show actual review dialog.
Should be implemented with Play Core Review API for production.

---

## SharedPreferences Keys

All stored in default SharedPreferences:

| Key | Type | Description |
|-----|------|-------------|
| `rotation_current_index` | Int | Current wallpaper index in rotation |
| `own_pro_unlock` | Boolean | Pro ownership status |
| `streak_last_apply` | String | ISO date of last apply |
| `streak_count` | Int | Current streak count |
| `review_applies_count` | Int | Total apply count for review gate |
| `review_shown` | Boolean | Whether review was shown |
| `auto_rotate` | Boolean | Auto-rotation enabled (Preferences) |
