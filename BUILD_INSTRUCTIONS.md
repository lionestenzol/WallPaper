# Build Instructions for focus-black

## Prerequisites

1. **Android Studio** Hedgehog (2023.1.1) or newer
2. **JDK 17** or newer
3. **Android SDK** with SDK Platform 36 (Android 15)
4. **Internet connection** (for first build to download dependencies)

## Quick Build (Debug APK)

### Option 1: Using Android Studio

1. Open the project in Android Studio:
   ```bash
   cd /home/user/WallPaper
   # Open in Android Studio: File → Open → select WallPaper folder
   ```

2. Wait for Gradle sync to complete (status bar at bottom)

3. Build the APK:
   - **Menu**: Build → Build Bundle(s) / APK(s) → Build APK(s)
   - **Shortcut**: Or just click the green ▶ Run button to build and install

4. Find the APK:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

### Option 2: Using Command Line

```bash
cd /home/user/WallPaper

# Make gradlew executable (if not already)
chmod +x gradlew

# Clean previous builds
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# APK will be at: app/build/outputs/apk/debug/app-debug.apk
```

### Option 3: Install Directly to Device

```bash
# Connect device via USB and enable USB debugging
adb devices  # Verify device is connected

# Build and install in one command
./gradlew installDebug

# The app will be installed and ready to launch
```

---

## Release Build (Production APK)

### Step 1: Create Keystore (First Time Only)

```bash
cd /home/user/WallPaper

# Generate keystore
keytool -genkey -v -keystore focus-black-release.keystore \
  -alias focus-black-key \
  -keyalg RSA -keysize 2048 -validity 10000

# Answer the prompts:
# - Password: [CHOOSE A STRONG PASSWORD]
# - Name: [Your name or company name]
# - Organizational Unit: [Your team/department]
# - Organization: [Your company]
# - City, State, Country: [Your location]
# - Confirm: yes
```

**IMPORTANT**:
- Keep the keystore file and password SECURE
- You MUST use the same keystore for all future app updates
- If you lose the keystore, you cannot update the app on Play Store

### Step 2: Configure Signing

Create `app/keystore.properties` with your keystore info:

```properties
storeFile=../focus-black-release.keystore
storePassword=YOUR_STORE_PASSWORD
keyAlias=focus-black-key
keyPassword=YOUR_KEY_PASSWORD
```

**Add to .gitignore** to keep credentials private:
```bash
echo "app/keystore.properties" >> .gitignore
echo "*.keystore" >> .gitignore
```

### Step 3: Update build.gradle

Add signing config to `app/build.gradle`:

```gradle
android {
    // ... existing config ...

    signingConfigs {
        release {
            def keystorePropertiesFile = rootProject.file("app/keystore.properties")
            if (keystorePropertiesFile.exists()) {
                def keystoreProperties = new Properties()
                keystoreProperties.load(new FileInputStream(keystorePropertiesFile))

                storeFile file(keystoreProperties['storeFile'])
                storePassword keystoreProperties['storePassword']
                keyAlias keystoreProperties['keyAlias']
                keyPassword keystoreProperties['keyPassword']
            }
        }
    }

    buildTypes {
        release {
            minifyEnabled true
            shrinkResources true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
            signingConfig signingConfigs.release  // Add this line
        }
    }
}
```

### Step 4: Build Release APK

```bash
# Clean previous builds
./gradlew clean

# Build release APK
./gradlew assembleRelease

# APK will be at: app/build/outputs/apk/release/app-release.apk
```

### Step 5: Verify the APK

```bash
# Check APK is signed
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk

# Should show: jar verified.

# Install on test device
adb install app/build/outputs/apk/release/app-release.apk
```

---

## Build App Bundle (For Play Store)

Play Store prefers **App Bundles (.aab)** over APKs for optimized downloads.

```bash
# Build release bundle
./gradlew bundleRelease

# Bundle will be at: app/build/outputs/bundle/release/app-release.aab
```

Upload `app-release.aab` to Google Play Console.

---

## Troubleshooting

### Problem: "SDK location not found"

**Solution**: Create `local.properties`:
```properties
sdk.dir=/path/to/Android/Sdk
```

On Linux/Mac:
```bash
echo "sdk.dir=$HOME/Android/Sdk" > local.properties
```

On Windows:
```
sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk
```

### Problem: "Execution failed for task ':app:lintVitalRelease'"

**Solution**: Disable lint checking in release builds.

Add to `app/build.gradle`:
```gradle
android {
    lintOptions {
        checkReleaseBuilds false
        abortOnError false
    }
}
```

### Problem: "Gradle sync failed: Connection timeout"

**Solutions**:
1. Check internet connection
2. Use Android Studio's built-in Gradle (File → Settings → Build → Gradle → Use Gradle from: 'wrapper')
3. Try offline mode: `./gradlew --offline assembleDebug`

### Problem: "Cannot find symbol" errors

**Solution**: Rebuild project
```bash
./gradlew clean build
```

Or in Android Studio: Build → Clean Project, then Build → Rebuild Project

---

## File Sizes (Approximate)

| Build Type | Size | Notes |
|------------|------|-------|
| Debug APK | 8-10 MB | Includes debugging symbols |
| Release APK | 4-6 MB | Minified with ProGuard |
| Release AAB | 3-5 MB | Play Store optimized |

---

## Next Steps After Building

1. **Test thoroughly** on real device
2. **Take screenshots** (8 screenshots minimum for Play Store)
3. **Create feature graphic** (1024x500px)
4. **Create hi-res icon** (512x512px)
5. **Write store description** (short: 80 chars, long: up to 4000 chars)
6. **Upload to Play Console** Internal Testing track
7. **Test with real users** (closed alpha/beta)
8. **Promote to Production** when ready

---

## Important Notes

### ProGuard Rules

The app includes ProGuard rules in `proguard-rules.pro`. If you encounter crashes in release builds, check logcat for ProGuard-related obfuscation issues.

### Version Management

Update version in `app/build.gradle` before each release:

```gradle
android {
    defaultConfig {
        versionCode 2      // Increment for each release
        versionName "1.1.0"  // Semantic versioning
    }
}
```

### Testing Checklist

Before releasing:

- [ ] Test on Android 8.0 (API 26) - minimum supported version
- [ ] Test on Android 15 (API 36) - target version
- [ ] Test on different screen sizes (phone, tablet)
- [ ] Test rotation (portrait, landscape)
- [ ] Test all wallpapers apply correctly
- [ ] Test auto-rotation (change date/time to trigger)
- [ ] Test manual rotation button
- [ ] Test widgets update after wallpaper change
- [ ] Test with battery saver enabled
- [ ] Test in-app review flow
- [ ] Verify no crashes in release build

---

## Build Automation (Optional)

Create `build-release.sh` for automated builds:

```bash
#!/bin/bash
set -e

echo "Building focus-black release APK..."

# Clean
./gradlew clean

# Build
./gradlew assembleRelease

# Sign & Align
APK="app/build/outputs/apk/release/app-release.apk"

if [ -f "$APK" ]; then
    echo "✓ Build successful!"
    echo "APK location: $APK"
    echo "Size: $(du -h $APK | cut -f1)"
else
    echo "✗ Build failed!"
    exit 1
fi
```

Make executable:
```bash
chmod +x build-release.sh
./build-release.sh
```

---

## Questions?

If you encounter issues not covered here:

1. Check Android Studio's Build Output window for detailed errors
2. Search the error message in Stack Overflow
3. Clean and rebuild: `./gradlew clean build`
4. File an issue at: [Your GitHub Repository]/issues
