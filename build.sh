#!/bin/bash

# Brickognize Build Script
# Simple helper script for common build tasks

set -e  # Exit on error

echo "🧱 Brickognize Build Helper"
echo "=========================="
echo ""

# Make sure we're in the right directory
if [ ! -f "settings.gradle.kts" ]; then
    echo "❌ Error: Not in project root directory"
    echo "Please run this script from the project root directory"
    echo "Tip: On Windows PowerShell/CMD, use .\\gradlew.bat instead of this script."
    exit 1
fi

# Make gradlew executable
chmod +x ./gradlew

# Show menu
echo "Select an option:"
echo "1. Build Debug APK"
echo "2. Build and Install Debug APK"
echo "3. Build Release APK"
echo "4. Clean Project"
echo "5. Clean and Rebuild"
echo "6. List All Tasks"
echo ""
read -p "Enter option (1-6): " option

case $option in
    1)
        echo ""
        echo "📦 Building debug APK..."
        ./gradlew :app:assembleDebug
        echo ""
        echo "✅ Done! APK location:"
        echo "   app/build/outputs/apk/debug/app-debug.apk"
        ;;
    2)
        echo ""
        echo "📦 Building and installing debug APK..."
        ./gradlew :app:installDebug
        echo ""
        echo "✅ Done! App installed on device."
        ;;
    3)
        echo ""
        echo "📦 Building release APK..."
        ./gradlew :app:assembleRelease
        echo ""
        echo "✅ Done! APK location:"
        echo "   app/build/outputs/apk/release/app-release-unsigned.apk"
        echo ""
        echo "⚠️  Note: This APK is unsigned and needs to be signed before installation."
        ;;
    4)
        echo ""
        echo "🧹 Cleaning project..."
        ./gradlew clean
        echo ""
        echo "✅ Done! Build artifacts cleaned."
        ;;
    5)
        echo ""
        echo "🧹 Cleaning and rebuilding..."
        ./gradlew clean :app:assembleDebug
        echo ""
        echo "✅ Done! APK location:"
        echo "   app/build/outputs/apk/debug/app-debug.apk"
        ;;
    6)
        echo ""
        ./gradlew tasks
        ;;
    *)
        echo ""
        echo "❌ Invalid option"
        exit 1
        ;;
esac

echo ""
