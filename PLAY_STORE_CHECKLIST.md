# Google Play Store Launch Checklist

## Pre-Submission Requirements

### ✅ Legal & Privacy (REQUIRED)

- [ ] **Privacy Policy Published**
  - Use the template in `PRIVACY_POLICY.md`
  - Host it publicly (GitHub Pages, your website, or use https://www.freeprivacypolicy.com/)
  - URL must be accessible and permanent

- [ ] **Content Rating Completed**
  - Go to Play Console → Content Rating
  - Answer questionnaire (app is rated for all ages)
  - No violence, gambling, or adult content

- [ ] **Data Safety Form Filled**
  - Play Console → Data Safety
  - Declare: "No data collected" (all data stays on device)
  - Link to privacy policy

### ✅ Store Listing

- [ ] **App Title** (max 50 characters)
  ```
  focus-black: Daily AMOLED Wallpapers
  ```

- [ ] **Short Description** (max 80 characters)
  ```
  Minimalist black wallpapers that auto-rotate daily. Pure AMOLED design.
  ```

- [ ] **Full Description** (max 4000 characters)
  ```
  Transform your phone screen with focus-black, a minimalist wallpaper app designed for AMOLED displays.

  🖤 PURE BLACK DESIGN
  All wallpapers feature true black (#000000) backgrounds that save battery on OLED screens and provide stunning contrast.

  🔄 AUTO-ROTATION
  Set it and forget it. Your wallpaper changes automatically every 24 hours, keeping your home screen fresh without any effort.

  ✨ KEY FEATURES
  • 7 handcrafted minimal wallpapers
  • Automatic daily rotation
  • Manual apply with one tap
  • Choose home screen, lock screen, or both
  • Battery-friendly rotation (delays when battery low)
  • Streak tracking to maintain your daily ritual
  • 4 home screen widgets
  • Pure black Material 3 design
  • No ads, no tracking, no data collection

  📱 WIDGETS
  • Focus Ring - Circular streak counter
  • Mode Sigil - Symbolic status indicator
  • Date Glyph - Minimalist date display
  • Battery Halo - Circular battery indicator

  🎨 WALLPAPER PACK: GENESIS
  1. Genesis Prime - Singular white dot
  2. Genesis Seal - Triangle symbol
  3. Horizon - Thin horizontal line
  4. Circle - Centered white circle
  5. Crescent - Moon-like crescent
  6. Grid - Subtle gray grid pattern
  7. Accent Line - Deep red vertical line

  ⚙️ SETTINGS
  • Auto-rotate toggle
  • Manual "rotate now" button
  • Choose wallpaper target (home/lock/both)
  • Battery optimization setting
  • Last apply status tracking

  🔒 PRIVACY
  focus-black respects your privacy:
  • All data stored locally on your device
  • No internet permissions (except for future remote packs)
  • No analytics or tracking
  • Open source - verify the code yourself

  💡 AMOLED OPTIMIZATION
  Designed specifically for OLED displays. Pure black pixels are completely off, saving battery and providing infinite contrast.

  🆓 100% FREE
  No in-app purchases, no subscriptions, no ads. Just clean, minimal wallpapers.

  📌 PERFECT FOR
  • Minimalists who appreciate clean design
  • AMOLED display owners (Samsung, Pixel, OnePlus, etc.)
  • Anyone who wants automatic wallpaper variety
  • Users who value privacy and simplicity
  • Dark mode enthusiasts

  🛠️ TECHNICAL DETAILS
  • Requires Android 8.0+ (API 26)
  • Optimized for Android 15
  • Uses WorkManager for reliable rotation
  • Material 3 design language
  • Fully offline after installation

  ⭐ COMING SOON
  • Additional wallpaper packs
  • Remote pack downloads
  • Custom rotation schedules
  • More widget designs

  Transform your screen. Focus on what matters. Go black.
  ```

- [ ] **Screenshots** (minimum 2, recommended 8)
  - Phone: 1080x2340 or similar 16:9/9:16 ratio
  - Take screenshots of:
    1. Today tab with wallpaper preview
    2. Apply button action (show wallpaper applied)
    3. Packs tab showing all 7 wallpapers
    4. Settings screen
    5. Widgets on home screen
    6. Different wallpapers applied
    7. Streak counter
    8. Dark mode UI

  **Screenshot Tips**:
  - Use a clean device (no notifications)
  - Show the app in use
  - Add captions explaining features
  - Use consistent device frame

- [ ] **Feature Graphic** (1024 x 500 px)
  ```
  Design requirements:
  - Pure black background (#000000)
  - App name "focus-black" in white
  - Tagline: "Daily AMOLED Ritual Wallpapers"
  - Show 2-3 wallpaper samples
  - Keep text readable at small sizes
  ```

- [ ] **App Icon** (512 x 512 px, high-res)
  ```
  Current icon is basic. Consider:
  - Pure black square with white minimal symbol
  - Matches app aesthetic
  - Distinctive at small sizes
  - No text (icon only)
  ```

### ✅ Technical Requirements

- [ ] **Release APK/AAB Built**
  - Follow instructions in `BUILD_INSTRUCTIONS.md`
  - Signed with your keystore
  - Version code: 1
  - Version name: 1.0.0

- [ ] **ProGuard Enabled**
  - Already configured in `build.gradle`
  - Minified for smaller file size
  - Tested that app works in release mode

- [ ] **Permissions Reviewed**
  - SET_WALLPAPER - Required
  - WAKE_LOCK - Required for WorkManager
  - INTERNET - Only if using remote packs (currently not needed)

- [ ] **Target SDK Updated**
  - Currently: SDK 36 (Android 15) ✓
  - Play Store requires recent SDK versions

### ✅ Testing Requirements

- [ ] **Device Testing**
  - [ ] Tested on real Android device (not just emulator)
  - [ ] Tested on Android 8.0 (minimum version)
  - [ ] Tested on Android 15 (target version)
  - [ ] Tested on different screen sizes
  - [ ] Tested rotation (portrait & landscape)
  - [ ] Tested with battery saver enabled
  - [ ] Verified all 7 wallpapers apply correctly
  - [ ] Verified widgets update after wallpaper change
  - [ ] Verified auto-rotation works (advance time 24 hours)

- [ ] **Crash Testing**
  - [ ] No crashes during normal use
  - [ ] No crashes when phone locked
  - [ ] No crashes with low battery
  - [ ] No crashes with airplane mode
  - [ ] No crashes when changing settings rapidly

- [ ] **In-App Review Flow Tested**
  - Apply wallpaper 3 times
  - Review dialog should appear
  - Test canceling and accepting

---

## Play Console Setup

### Step 1: Create App

1. Go to https://play.google.com/console
2. Click "Create app"
3. Fill in:
   - **App name**: focus-black
   - **Default language**: English (United States)
   - **App or game**: App
   - **Free or paid**: Free
   - Check declarations and click "Create app"

### Step 2: Set Up Your App

Complete all required sections in Play Console:

#### App Access
- [ ] All functionality is available without special access
- [ ] No login required
- [ ] No restrictions

#### Ads
- [ ] Does your app contain ads? **NO**

#### Content Rating
- [ ] Complete questionnaire
- [ ] Select "Everyone" rating
- [ ] No violence, gambling, or adult content

#### Target Audience
- [ ] Age group: All ages
- [ ] Appeal to children: No

#### News Apps
- [ ] Not a news app

#### COVID-19 Contact Tracing and Status Apps
- [ ] Not a COVID app

#### Data Safety
- [ ] Does your app collect or share user data? **NO**
- [ ] All data stays on device
- [ ] Link privacy policy: `https://your-domain.com/privacy-policy`

#### App Category
- [ ] **Category**: Personalization
- [ ] **Tags**: Wallpaper, Minimal, AMOLED, Dark

#### Store Listing Contact Details
- [ ] Email: your-email@example.com
- [ ] Optional: Phone, website

### Step 3: Internal Testing (RECOMMENDED)

Before going live, test with internal testers:

1. **Create Internal Test Release**
   - Play Console → Testing → Internal testing
   - Upload your AAB file
   - Add release notes

2. **Add Test Users**
   - Add email addresses of testers
   - They'll receive link to install

3. **Test for 1-2 weeks**
   - Fix any bugs found
   - Update screenshots if needed
   - Iterate until stable

### Step 4: Production Release

When ready to launch:

1. **Create Production Release**
   - Play Console → Production → Create new release
   - Upload final AAB
   - Write release notes

2. **Roll Out Strategy**
   - Start with 10% rollout
   - Monitor crashes and reviews
   - Increase to 50% after 2-3 days
   - Full 100% rollout after 1 week

3. **Submit for Review**
   - Click "Submit for review"
   - Google will review (usually 1-3 days)
   - May request changes

---

## Post-Launch Checklist

### Week 1
- [ ] Monitor crash reports daily
- [ ] Respond to user reviews within 24 hours
- [ ] Check Play Console for policy warnings
- [ ] Track downloads and retention

### Week 2-4
- [ ] Analyze user feedback
- [ ] Plan first update based on feedback
- [ ] Consider adding new wallpaper packs
- [ ] Optimize for better retention

### Ongoing
- [ ] Update app every 3-6 months (Play Store requirement)
- [ ] Keep target SDK updated (Google policy)
- [ ] Add new features based on user requests
- [ ] Monitor and fix crashes

---

## Common Rejection Reasons

### 1. Privacy Policy Issues
**Problem**: Privacy policy not accessible or incomplete
**Solution**: Host on permanent URL, cover all Play Store requirements

### 2. Misleading Screenshots
**Problem**: Screenshots show features not in app
**Solution**: Only show actual app functionality

### 3. Icon Issues
**Problem**: Icon too similar to Android system icons
**Solution**: Use unique, distinctive icon

### 4. Metadata Issues
**Problem**: Description contains prohibited content (competitor names, prices, etc.)
**Solution**: Remove competitor mentions, pricing info

### 5. Target SDK Too Old
**Problem**: Targeting old Android version
**Solution**: We're targeting SDK 36 (Android 15) ✓

---

## Marketing Checklist (Optional)

Once live on Play Store:

- [ ] Share on social media (Twitter, Reddit /r/Android, /r/androidapps)
- [ ] Post on Product Hunt
- [ ] Submit to Android Authority, Android Police for review
- [ ] Create demo video for YouTube
- [ ] Write blog post about development
- [ ] Add "Available on Google Play" badge to website/GitHub
- [ ] Enable Google Play Console pre-registration (for updates)

### Play Store Badge

```html
<a href='https://play.google.com/store/apps/details?id=com.focusblack.wallos'>
  <img alt='Get it on Google Play'
       src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png'
       width='200'/>
</a>
```

---

## Monetization Ideas (Future)

If you want to add revenue later:

1. **Freemium Model**
   - Free: 7 wallpapers, basic features
   - Pro ($2.99): More packs, custom schedules, themes

2. **Wallpaper Packs**
   - $0.99 per additional pack
   - Seasonal packs (holidays, seasons)
   - Artist collaborations

3. **Ad-Supported Free Version**
   - Respectful banner ad in Settings
   - No ads in main wallpaper view
   - Pro version removes ads

4. **Donations**
   - "Support Development" button
   - Ko-fi, Buy Me a Coffee links

**Current Status**: 100% free, no monetization ✓

---

## Timeline Estimate

| Phase | Duration | Tasks |
|-------|----------|-------|
| Preparation | 2-4 hours | Screenshots, graphics, descriptions |
| Play Console Setup | 1-2 hours | Fill all required fields |
| Internal Testing | 1-2 weeks | Fix bugs, iterate |
| Review | 1-3 days | Google reviews app |
| Live on Play Store | Instant | After approval |

**Total Time to Launch**: 2-3 weeks (including testing)

---

## Success Metrics

Track these in Play Console:

- **Installs**: Downloads per day/week/month
- **Uninstalls**: Why users are leaving
- **Crashes**: ANR rate, crash-free users %
- **Ratings**: Average star rating, review count
- **Retention**: D1, D7, D30 retention rates

**Realistic Goals for First Month**:
- 50-500 installs (organic)
- 4.0+ star rating
- <1% crash rate
- 30%+ D1 retention

---

## Help & Support

If you get stuck:

1. **Play Console Help**: https://support.google.com/googleplay/android-developer
2. **Policy Center**: https://play.google.com/about/developer-content-policy/
3. **Developer Forums**: https://support.google.com/googleplay/android-developer/community
4. **Stack Overflow**: Tag [google-play-console]

Good luck with your launch! 🚀
