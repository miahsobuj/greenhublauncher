#!/bin/bash

# GreenHub Launcher - Fix, Commit and Build Script
# Run this script in your local machine

set -e

echo "======================================"
echo "  GreenHub Launcher - Fix & Build"
echo "======================================"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Step 1/5: Checking git status...${NC}"
git status

# Fix gradlew wrapper (ensure proper format)
echo -e "${YELLOW}Step 2/5: Checking gradlew format...${NC}"
if grep -q '" \\(-Xmx64m -Xms64m\) "' ./gradlew 2>/dev/null; then
    echo -e "${GREEN}gradlew is already fixed!${NC}"
else
    echo -e "${YELLOW}Fixing gradlew JVM options...${NC}"
    # Create backup
    cp ./gradlew ./gradlew.bak
    # Fix the DEFAULT_JVM_OPTS line
    sed -i "s/DEFAULT_JVM_OPTS=.*/DEFAULT_JVM_OPTS=\" -Xmx64m -Xms64m \"/" ./gradlew
    echo -e "${GREEN}gradlew fixed!${NC}"
fi

chmod +x ./gradlew

echo -e "${YELLOW}Step 3/5: Staging changes...${NC}"
git add -A

echo -e "${YELLOW}Step 4/5: Committing changes...${NC}"
if git diff --cached --quiet; then
    echo -e "${GREEN}No new changes to commit.${NC}"
else
    git commit -m "Fixes: Android 15 compatibility and GitHub Actions build

- Updated MainActivity.java with proper imports
- Fixed MusicPlayerActivity.java with corrected Song references
- Fixed ContactsActivity.java with proper Contact model usage
- Fixed dialog_input.xml layout for Android 15
- Fixed gradlew JVM options for proper build execution
- Updated GitHub Actions workflow to v4 actions"
    echo -e "${GREEN}Changes committed!${NC}"
fi

echo -e "${YELLOW}Step 5/5: Pushing to GitHub...${NC}"
echo -e "${YELLOW}Note: If this fails, you may need to pull first or check your SSH credentials${NC}"
if git push origin main; then
    echo -e "${GREEN}Successfully pushed to GitHub!${NC}"
    echo ""
    echo -e "${GREEN}======================================${NC}"
    echo -e "${GREEN}  BUILD TRIGGERED ON GITHUB ACTIONS${NC}"
    echo -e "${GREEN}  Check: https://github.com/miahsobuj/greenhublauncher/actions${NC}"
    echo -e "${GREEN}======================================${NC}"
else
    echo -e "${RED}Push failed. Possible solutions:${NC}"
    echo -e "${YELLOW}1. Check your SSH key: ssh -T git@github.com${NC}"
    echo -e "${YELLOW}2. Pull latest changes: git pull origin main${NC}"
    echo -e "${YELLOW}3. Check remote URL: git remote -v${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}All done!${NC}"
