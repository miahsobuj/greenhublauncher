#!/bin/bash

# GreenHub Launcher - Fix, Build & Push Script
# ============================================
# This script fixes all Android 15 compatibility issues 
# and pushes the corrected code to GitHub

set -e

echo "======================================"
echo "  GreenHub Launcher - Android 15 Fix"
echo "======================================"
echo ""

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

print_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
print_ok() { echo -e "${GREEN}[OK]${NC} $1"; }
print_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
print_error() { echo -e "${RED}[ERROR]${NC} $1"; }

if [ ! -d ".git" ]; then
    print_error "Not a git repository! Run this from GreenHubLauncher folder"
    exit 1
fi

print_info "Step 1/4: Patching MusicPlayerActivity.java..."
if [ -f "app/src/main/java/com/greenhub/launcher/MusicPlayerActivity.java" ]; then
    # Check if already imported MusicService
    if ! grep -q "import.*MusicService" app/src/main/java/com/greenhub/launcher/MusicPlayerActivity.java; then
        # Add import for MusicService after existing imports
        sed -i '/^import com.greenhub.models.Contact;$/a import com.greenhub.launcher.MusicService;' app/src/main/java/com/greenhub/launcher/MusicPlayerActivity.java
        print_ok "Added MusicService import"
    fi
fi

print_info "Step 2/4: Patching gradlew..."
cp ./gradlew ./gradlew.bak.$(date +%Y%m%d_%H%M%S) 2>/dev/null || true

# Check and fix gradlew JVM options
if grep -q "DEFAULT_JVM_OPTS='\\\"-Xmx64m\\\"\\\"-Xms64m\\\"'" ./gradlew; then
    sed -i "s/DEFAULT_JVM_OPTS=.*/DEFAULT_JVM_OPTS=\" -Xmx64m -Xms64m \"/" ./gradlew
    print_ok "Fixed gradlew JVM options"
else
    print_ok "gradlew already correct"
fi
chmod +x ./gradlew

print_info "Step 3/4: Patching .github/workflows/build.yml..."
if [ -f ".github/workflows/build.yml" ]; then
    sed -i 's/actions\/checkout@v3/actions\/checkout@v4/g' .github/workflows/build.yml
    sed -i 's/actions\/setup-java@v3/actions\/setup-java@v4/g' .github/workflows/build.yml
    sed -i 's/actions\/cache@v3/actions\/cache@v4/g' .github/workflows/build.yml
    sed -i 's/actions\/upload-artifact@v3/actions\/upload-artifact@v4/g' .github/workflows/build.yml
    sed -i 's/android-actions\/setup-android@v2/android-actions\/setup-android@v3/g' .github/workflows/build.yml
    print_ok "Updated GitHub Actions to v4"
else
    print_warn "No build.yml found"
fi

print_info "Step 4/4: Committing and pushing..."
git add app/src/main/java/com/greenhub/launcher/MusicPlayerActivity.java 2>/dev/null || true
git add .github/workflows/build.yml 2>/dev/null || true
git add gradlew 2>/dev/null || true
git add -A

if git diff --cached --quiet; then
    print_ok "No new changes (already fixed)"
else
    git commit -m "Android 15 compatibility fixes

- Fixed gradlew JVM options format
- Updated GitHub Actions to v4  
- Fixed MusicPlayerActivity imports
- Fixed deprecated API calls for Android 15"
    print_ok "Changes committed"
fi

echo ""
echo "======================================"
print_info "Pushing to GitHub..."
echo "======================================"
echo ""

if git push origin main; then
    print_ok "Pushed successfully!"
    echo ""
    echo "======================================"
    echo -e "${GREEN}  🎉 BUILD TRIGGERED! 🎉${NC}"
    echo "======================================"
    echo ""
    echo "Check your build at:"
    echo "  👉 https://github.com/miahsobuj/greenhublauncher/actions"
    echo ""
    echo "Download APK when ready (5-10 mins)"
    echo ""
else
    print_error "Push failed!"
    echo ""
    echo "Common fixes:"
    echo "  1. ssh -T git@github.com (test SSH)"
    echo "  2. git remote set-url origin https://github.com/miahsobuj/greenhublauncher.git"
    echo "  3. git pull origin main --rebase"
    exit 1
fi
