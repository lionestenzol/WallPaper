#!/bin/bash
echo "Building focus-black APK..."
echo "This requires internet connection to download dependencies"
echo ""

# Check if gradle wrapper exists
if [ ! -f "./gradlew" ]; then
    echo "Gradle wrapper not found. Generating..."
    gradle wrapper
fi

# Build debug APK
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Build successful!"
    echo "APK location: app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    echo "To install on device:"
    echo "  adb install app/build/outputs/apk/debug/app-debug.apk"
else
    echo ""
    echo "❌ Build failed. Check errors above."
fi
