# GreenHub Launcher - Android Project

## Quick Start Guide

### Prerequisites
- Android Studio (latest version recommended)
- Java 17 or higher
- Android SDK API 24+ (Android 7.0+)

### Building the APK

#### Method 1: Android Studio
1. Open Android Studio
2. Select "Open an existing project"
3. Choose the GreenHubLauncher folder
4. Wait for Gradle sync to complete
5. Click Build → Build Bundle(s) / APK(s) → Build APK(s)
6. Find the APK at: `app/build/outputs/apk/debug/app-debug.apk`

#### Method 2: Command Line
```bash
# Navigate to project directory
cd GreenHubLauncher

# Build debug APK
./gradlew assembleDebug

# Build release APK  
./gradlew assembleRelease
```

### Installing the Launcher
1. Transfer APK to your Android device
2. Enable "Install from Unknown Sources" in Settings
3. Install the APK
4. When prompted, select "GreenHub Launcher" as default home

### Project Features
- ✅ Launcher Home with App Drawer
- ✅ File Manager with storage access
- ✅ Scientific Calculator
- ✅ Web Browser
- ✅ Notepad with database storage
- ✅ Video Downloader (using Download Manager)
- ✅ Music Player with playlists
- ✅ Phone Dialer
- ✅ Contacts viewer
- ✅ Beautiful Green Theme

### Folder Structure
```
app/src/main/
├── java/com/greenhub/launcher/
│   ├── MainActivity.java - Launcher Home
│   ├── FileManagerActivity.java
│   ├── CalculatorActivity.java
│   ├── BrowserActivity.java
│   ├── NotepadActivity.java
│   ├── VideoDownloaderActivity.java
│   ├── MusicPlayerActivity.java
│   ├── PhoneActivity.java
│   ├── ContactsActivity.java
│   ├── models/ - Data models (AppInfo, FileItem, Note, Contact)
│   ├── adapters/ - RecyclerView adapters
│   └── utils/ - Helper classes
├── res/
│   ├── drawable/ - Icons and backgrounds
│   ├── layout/ - UI layouts
│   └── values/ - Colors, strings, styles, themes
└── AndroidManifest.xml
```

### Customization
- Edit `res/values/colors.xml` to change the green color scheme
- Edit `res/values/strings.xml` to modify text labels
- Add new activities in `java/com/greenhub/launcher/`

### Troubleshooting
**Build fails?**
- Check SDK version in build.gradle matches your installed SDK
- Try: File → Invalidate Caches / Restart
- Check internet connection for Gradle dependencies

**App doesn't appear as launcher option?**
- Check AndroidManifest.xml has LAUNCHER/HOME intent filter
- Install app and long press home button to select launcher

**Storage permissions not working?**
- On Android 11+, requires "All Files Access" permission
- Check Settings → Apps → GreenHub Launcher → Permissions

### License
This project is open source. Feel free to modify and distribute.

---

Happy building! 🚀
