#!/bin/bash

# GreenHub Launcher - Complete Fix, Build & Push Script
# =====================================================
# This script prepares your project for building on GitHub Actions
# Run this in your terminal after cloning the repository

set -e

echo "======================================"
echo "  GreenHub Launcher - Build Prep"
echo "======================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if we're in a git repository
if [ ! -d ".git" ]; then
    print_error "Not a git repository! Make sure you're in the GreenHubLauncher directory."
    exit 1
fi

print_status "Step 1/6: Verifying project structure..."

# Check for required files
REQUIRED_FILES=("gradlew" "build.gradle" "settings.gradle" ".github/workflows/build.yml")
for file in "${REQUIRED_FILES[@]}"; do
    if [ ! -f "$file" ]; then
        print_error "Missing file: $file"
        exit 1
    fi
done
print_success "Project structure verified"

echo ""
print_status "Step 2/6: Fixing gradlew wrapper..."

# Create backup
cp ./gradlew ./gradlew.bak.$(date +%Y%m%d_%H%M%S) 2>/dev/null || true

# Fix gradlew JVM options to ensure proper quoting
if grep -q 'DEFAULT_JVM_OPTS="' ./gradlew; then
    # Check if it's already fixed (single quotes should not be present in a problematic way)
    if grep 'DEFAULT_JVM_OPTS=' ./gradlew | grep -q "'\\\""; then
        print_warning "Found old problematic format, fixing..."
        # Fix the problematic format
        sed -i 's/DEFAULT_JVM_OPTS=.*/DEFAULT_JVM_OPTS=" -Xmx64m -Xms64m "/' ./gradlew
        chmod +x ./gradlew
        print_success "gradlew fixed"
    else
        chmod +x ./gradlew
        print_success "gradlew is already in correct format"
    fi
else
    chmod +x ./gradlew
    print_success "gradlew executable permissions set"
fi

echo ""
print_status "Step 3/6: Updating GitHub Actions workflow..."

# Update build.yml to use latest actions versions
if [ -f ".github/workflows/build.yml" ]; then
    # Update to v4 actions
    sed -i 's/actions\/checkout@v[0-9]*/actions\/checkout@v4/g' .github/workflows/build.yml
    sed -i 's/actions\/setup-java@v[0-9]*/actions\/setup-java@v4/g' .github/workflows/build.yml  
    sed -i 's/actions\/cache@v[0-9]*/actions\/cache@v4/g' .github/workflows/build.yml
    sed -i 's/actions\/upload-artifact@v[0-9]*/actions\/upload-artifact@v4/g' .github/workflows/build.yml
    sed -i 's/android-actions\/setup-android@v[0-9]*/android-actions\/setup-android@v3/g' .github/workflows/build.yml
    print_success "GitHub Actions workflow updated to latest versions"
fi

echo ""
print_status "Step 4/6: Staging all changes..."

# Add all changes
git add -A

# Show what's being committed
echo ""
echo "Files staged for commit:"
git diff --cached --name-only --diff-filter=ACM 2>/dev/null | head -20 || echo "(no changes)"

echo ""
print_status "Step 5/6: Committing changes..."

if git diff --cached --quiet; then
    print_warning "No new changes to commit (code may already be updated)"
else
    git commit -m "Android 15 Compatibility Fixes

Changes:
- Updated MainActivity imports and structure
- Fixed MusicPlayerActivity Song class references  
- Fixed ContactsActivity Contact model usage
- Fixed dialog_input.xml layout for Android 15
- Fixed gradlew JVM options for proper execution
- Updated GitHub Actions to v4 action versions"
    print_success "Changes committed successfully"
fi

echo ""
echo "======================================"
print_status "Step 6/6: Pushing to GitHub..."
echo "======================================"
echo ""

# Try to push
echo "Attempting to push to origin/main..."
if git push origin main 2>&amp;1; then
    print_success "Successfully pushed to GitHub!"
    echo ""
    echo "======================================"
    echo -e "${GREEN}  🎉 BUILD TRIGGERED SUCCESSFULLY! 🎉${NC}"
    echo "======================================"
    echo ""
    echo "Your APK will be built automatically on GitHub Actions."
    echo ""
    echo "Check build status:"
    echo "  👉 https://github.com/miahsobuj/greenhublauncher/actions"
    echo ""
    echo "Download your APK here when ready:"
    echo "  👉 https://github.com/miahsobuj/greenhublauncher/actions/workflows/build.yml"
    echo ""
    echo "Build typically takes 5-10 minutes."
    echo ""
else
    print_error "Push failed! Possible issues:"
    echo ""
    echo "1. SSH Authentication issue:"
    echo "   Test with: ssh -T git@github.com"
    echo "   If it fails, add your SSH key:"
    echo "   ssh-add ~/.ssh/id_rsa"
    echo ""
    echo "2. HTTPS instead of SSH:"
    echo "   git remote set-url origin https://github.com/miahsobuj/greenhublauncher.git"
    echo ""
    echo "3. Need to pull first:"
    echo "   git pull origin main --rebase"
    echo ""
    echo "4. Check remote URL:"
    echo "   git remote -v"
    echo ""
    echo "Current remote:"
    git remote -v
    echo ""
    exit 1
fi

echo ""
echo "======================================"
echo "             🎊 DONE! 🎊"
echo "======================================"
echo ""
