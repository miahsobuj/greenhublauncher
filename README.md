# 🚀 GreenHub Launcher - Android Source Code

A **feature-rich Android Launcher** with built-in utilities, created for you with a beautiful green aesthetic theme!

---

## 📱 Features Included

### 🏠 Core Launcher
- App drawer with search
- Beautiful home screen with date/time
- App shortcuts and quick actions

### 🛠️ Built-in Tools

| Tool | Description |
|------|-------------|
| 📁 **File Manager** | Browse, copy, move, delete files with full storage access |
| 🧮 **Calculator** | Scientific calculator with advanced operations |
| 🌐 **Browser** | Full web browser with dark mode support |
| 📝 **Notepad** | Notes with save/edit/delete functionality |
| 📥 **Video Downloader** | Download from YouTube, Instagram, etc (using Download Manager) |
| 🎵 **Music Player** | Audio player with playlist and equalizer |
| 📞 **Phone** | Dialer for making calls |
| 👥 **Contacts** | Full contact management |

### 🎨 Theme
- **Green aesthetic** with dark mode
- Material Design 3 components
- Glass-morphism UI elements
- Gradient backgrounds

---

## 📂 Project Structure

```
GreenHubLauncher/
├── app/
│   ├── src/main/
│   │   ├── java/com/greenhub/launcher/
│   │   │   ├── MainActivity.java          # Launcher Home
│   │   │   ├── FileManagerActivity.java   # File Manager
│   │   │   ├── CalculatorActivity.java    # Calculator
│   │   │   ├── BrowserActivity.java       # Web Browser
│   │   │   ├── NotepadActivity.java       # Notes
│   │   │   ├── VideoDownloaderActivity.java # Video DL
│   │   │   ├── MusicPlayerActivity.java   # Music Player
│   │   │   ├── PhoneActivity.java         # Dialer
│   │   │   ├── ContactsActivity.java      # Contacts
│   │   │   ├── models/
│   │   │   │   ├── AppInfo.java
│   │   │   │   ├── FileItem.java
│   │   │   │   ├── Note.java
│   │   │   │   └── Contact.java
│   │   │   ├── adapters/
│   │   │   │   ├── AppAdapter.java
│   │   │   │   └── ToolsPagerAdapter.java
│   │   │   └── utils/
│   │   │       ├── FileUtils.java
│   │   │       └── NoteManager.java
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   ├── drawable/
│   │   │   └── values/
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
├── settings.gradle
└── gradle.properties
```

---

## 🔧 Build Instructions

### Option 1: Android Studio (Recommended)

1. **Download Android Studio** from https://developer.android.com/studio

2. **Import the project:**
   - Open Android Studio
   - Click "Open an existing project"
   - Select the `GreenHubLauncher` folder
   - Wait for Gradle sync

3. **Build the APK:**
   - Click `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`
   - OR go to `Build` → `Generate Signed Bundle / APK`

4. **Install on device:**
   - Connect your Android device via USB (enable USB debugging)
   - Click the Run button (▶️) or press Shift+F10

### Option 2: Command Line (Gradle)

```bash
# Navigate to project folder
cd GreenHubLauncher

# Make gradlew executable (Linux/Mac)
chmod +x gradlew

# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing config)
./gradlew assembleRelease
```

### Option 3: Use AIDE (Android IDE) on Phone

1. Install **AIDE - Android IDE** from Play Store
2. Copy project files to phone
3. Open in AIDE and build

### Option 4: Termux (Advanced)

```bash
# Install Termux from F-Droid
# Then install Android SDK tools

apt update
apt install openjdk-17 gradle

# Build the project
cd GreenHubLauncher
gradle assembleDebug
```

---

## ⚙️ Configuration

### Required Permissions
The app requests these permissions automatically:
- `READ_EXTERNAL_STORAGE` - File manager access
- `WRITE_EXTERNAL_STORAGE` - File operations
- `CALL_PHONE` - Dialer functionality
- `READ_CONTACTS` - Contacts access
- `INTERNET` - Browser and downloads
- `RECORD_AUDIO` - Music player visualizer

### For Android 11+ (Scoped Storage)
The app will redirect to system settings to grant "All files access" permission.

---

## 🎨 Customization

### Change Colors
Edit `res/values/colors.xml`:
```xml
<color name="primary_green">#2E7D32</color>
<color name="accent_green">#66BB6A</color>
```

### Add More Tools
1. Create new Activity in `.../activity/` folder
2. Add layout in `res/layout/`
3. Register in `AndroidManifest.xml`
4. Update `ToolsPagerAdapter.java`

---

## 📦 Dependencies

```gradle
- Material Design 3 (com.google.android.material:material:1.11.0)
- AndroidX AppCompat
- RecyclerView & CardView
- WebKit (for WebView)
- Media (for Music Player)
- Volley & OkHttp (for downloads)
```

---

## 🐛 Troubleshooting

| Issue | Solution |
|-------|----------|
| Gradle sync fails | Check internet, try File → Invalidate Caches |
| Build fails | Check SDK version matches in build.gradle |
| App not as launcher | Check `AndroidManifest.xml` intent filters |
| Storage permission denied | Grant All Files Access on Android 11+ |

---

## 📜 Notes

### Video Downloader
The video downloader uses Android's Download Manager. For actual YouTube/Instagram downloads, integration with yt-dlp or similar would be needed (not included for legal reasons).

### Music Player
Basic player functionality is included. Full equalizer implementation requires AudioEffect API and may vary by device.

### Permissions
Some features require runtime permissions. The app handles permission requests automatically.

---

## 📝 License

This code is provided as-is for educational purposes. Feel free to modify and distribute.

---

## 🙏 Credits

Built with ❤️ using Android SDK and Material Design 3

---

## 💡 Next Steps

1. Build the APK following instructions above
2. Install on your Android device
3. Set as default launcher when prompted
4. Enjoy your custom GreenHub Launcher!

**Questions?** Check the Android Developer documentation: https://developer.android.com
