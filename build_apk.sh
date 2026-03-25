#!/bin/bash

# GreenHub Launcher - Build Script
# This script can be used with a local Android SDK
# Or in CI/CD environments like GitHub Actions

set -e

echo "========================================="
echo "   GreenHub Launcher Build Script"
echo "========================================="

# Check Java
if ! command -v java &> /dev/null; then
    echo "❌ Java not found. Please install Java 17:"
    echo "   sudo apt install openjdk-17-jdk"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
# Accept arm64 as alternative to aarch64 for Android NDK
export NDK_HOST_TAG=linux-arm64
echo "✅ Java version: $JAVA_VERSION"

# Check Android SDK
if [ -z "$ANDROID_SDK_ROOT" ] && [ -z "$ANDROID_HOME" ]; then
    echo "⚠️  ANDROID_SDK_ROOT not set."
    echo "   Please set it: export ANDROID_SDK_ROOT=/path/to/android-sdk"
    echo ""
    echo "📦 To download Android SDK:"
    echo "   https://developer.android.com/studio#command-line-tools-only"
    exit 1
fi

echo "✅ Android SDK found at: ${ANDROID_SDK_ROOT:-$ANDROID_HOME}"

# Navigate to project
cd "$(dirname "$0")"

# Check if wrapper exists, if not create it
if [ ! -f "./gradlew" ]; then
    echo "⚠️  Gradle wrapper not found. Downloading..."
    
    # Create Gradle wrapper
    mkdir -p gradle/wrapper
    
    cat > gradle/wrapper/gradle-wrapper.properties << 'EOF'
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.2-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
EOF
    
    # Download gradlew
    wget -q https://raw.githubusercontent.com/gradle/gradle/v8.2.0/gradlew/gradlew -O gradlew || true
    chmod +x gradlew 2>/dev/null || true
fi

# Build APK
echo ""
echo "🔨 Building APK..."
echo ""

if [ -f "./gradlew" ]; then
    ./gradlew assembleDebug
else
    gradle assembleDebug
fi

# Check result
if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Build successful!"
    echo ""
    echo "📱 APK Location:"
    echo "   app/build/outputs/apk/debug/app-debug.apk"
else
    echo ""
    echo "❌ Build failed. Check errors above."
fi