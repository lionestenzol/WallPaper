# Play Store Setup Guide

Complete guide for publishing focus-black to Google Play Store.

## Prerequisites

- Google Play Developer account ($25 one-time fee)
- Signed release APK or AAB
- App icon (512x512 PNG)
- Feature graphic (1024x500 PNG)
- Screenshots (minimum 2, phone and tablet recommended)
- Privacy policy URL

## Step 1: Create App in Play Console

1. Go to [Google Play Console](https://play.google.com/console)
2. Click **Create app**
3. Fill in app details:
   - **App name:** focus-black
   - **Default language:** English (US)
   - **App or game:** App
   - **Free or paid:** Free (with in-app purchases)
4. Accept declarations and click **Create app**

## Step 2: Store Listing

### Main Store Listing

Navigate to **Grow > Store presence > Main store listing**

**Short description (80 chars max):**
```
AMOLED wallpapers with daily auto-rotation and streak tracking
```

**Full description (4000 chars max):**
```
focus-black is a minimalist wallpaper app designed for AMOLED displays.

FEATURES:
• Pure black wallpapers - Save battery on OLED/AMOLED screens
• Daily auto-rotation - Fresh wallpaper every morning
• Streak tracking - Build your daily wallpaper habit
• 7 minimal designs - Clean, distraction-free aesthetics
• One-tap apply - Change your wallpaper instantly

GENESIS PACK INCLUDED:
The Genesis Pack features 7 carefully crafted wallpapers with pure black backgrounds and subtle white or accent elements. Perfect for AMOLED displays.

PRO UNLOCK:
Unlock Pro for access to all current and future wallpaper packs with a single purchase.

PERMISSIONS:
• Set Wallpaper - Required to change your device wallpaper
• Wake Lock - For reliable background rotation

No ads. No tracking. Just beautiful wallpapers.
```

### Graphics

Upload required assets:
- **App icon:** 512x512 PNG (no transparency)
- **Feature graphic:** 1024x500 PNG
- **Screenshots:** At least 2 phone screenshots

### Categorization

- **App category:** Personalization
- **Tags:** Wallpaper, AMOLED, Dark theme, Minimalist

## Step 3: App Content

Navigate to **Policy > App content**

Complete all declarations:

### Privacy Policy
- Host privacy policy at a public URL
- Enter URL in Play Console
- Must cover data collection (app collects minimal data)

### Ads
- Select **No, my app does not contain ads**

### App Access
- Select **All functionality is available without special access**

### Content Rating
- Complete IARC questionnaire
- App should receive **Everyone** rating

### Target Audience
- Select **18 and over** (simplest option)

### News Apps
- Select **No**

### COVID-19 Apps
- Select **No**

### Data Safety

Declare what data the app collects:

| Data Type | Collected | Shared | Required |
|-----------|-----------|--------|----------|
| Purchase history | Yes | No | Yes |
| App interactions | No | No | - |
| Crash logs | No | No | - |

Note: If using analytics in future, update this section.

## Step 4: In-App Products

Navigate to **Monetize > Products > In-app products**

### Create Pro Unlock Product

1. Click **Create product**
2. Fill in details:
   - **Product ID:** `pro_unlock`
   - **Name:** Pro Unlock
   - **Description:** Unlock all wallpaper packs forever
   - **Default price:** $2.99 (or your preferred price)
3. Click **Save**
4. Click **Activate**

### Pricing

Set prices for all countries or use auto-conversion:
- Base price: $2.99 USD
- Let Google auto-convert for other regions

## Step 5: Testing

### Internal Testing Track

1. Navigate to **Release > Testing > Internal testing**
2. Click **Create new release**
3. Upload signed AAB file
4. Add release notes
5. Click **Save** then **Review release** then **Start rollout**

### Add Testers

1. Navigate to **Release > Testing > Internal testing > Testers**
2. Create email list with tester emails
3. Share opt-in link with testers

### License Testing

For testing purchases without real charges:

1. Navigate to **Settings > License testing**
2. Add tester email addresses
3. Set license response to **RESPOND_NORMALLY**

Testers can now make purchases that auto-refund.

## Step 6: Release

### Production Release

1. Complete all policy compliance items
2. Navigate to **Release > Production**
3. Click **Create new release**
4. Upload signed AAB
5. Add release notes
6. Submit for review

### Review Process

- Initial review: 1-7 days
- Updates: Usually faster
- May request additional information

## Step 7: Post-Launch

### Monitor

- Check **Quality > Android vitals** for crashes
- Review **Ratings and reviews**
- Monitor **Statistics** for installs

### Updates

1. Increment `versionCode` and `versionName` in build.gradle
2. Build signed AAB
3. Create new release in Production track
4. Submit for review

## Signing

### Generate Keystore (First Time)

```bash
keytool -genkey -v -keystore focus-black.keystore -alias focus-black -keyalg RSA -keysize 2048 -validity 10000
```

### Configure Signing in build.gradle

```gradle
android {
    signingConfigs {
        release {
            storeFile file("../focus-black.keystore")
            storePassword "your-store-password"
            keyAlias "focus-black"
            keyPassword "your-key-password"
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
            // ... other config
        }
    }
}
```

**IMPORTANT:** Never commit keystore or passwords to version control.

### Build Release AAB

```bash
./gradlew bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`

## Troubleshooting

### Billing Not Working

1. Ensure app is uploaded to Play Console (any track)
2. Product must be **Active**
3. Tester email must be in license testers
4. Use signed release build (not debug)
5. Wait 24 hours after creating product

### Review Rejection

Common reasons:
- Missing privacy policy
- Incomplete store listing
- Policy violations

Read rejection email carefully and address specific issues.

## Checklist

Before submitting:

- [ ] App icon uploaded (512x512)
- [ ] Feature graphic uploaded (1024x500)
- [ ] At least 2 screenshots uploaded
- [ ] Short description filled
- [ ] Full description filled
- [ ] Privacy policy URL added
- [ ] All content declarations complete
- [ ] In-app product created and activated
- [ ] Signed AAB uploaded
- [ ] Release notes added
- [ ] All countries selected for distribution
