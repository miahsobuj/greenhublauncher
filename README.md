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


---

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
