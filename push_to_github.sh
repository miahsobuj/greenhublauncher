#!/bin/bash
# Run this script in the GreenHubLauncher directory to push changes to GitHub

cd "$(dirname "$0")"

git push origin main
echo "Changes pushed successfully!"
