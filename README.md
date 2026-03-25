# GreenHub Launcher

A feature-rich, all-in-one Android launcher that combines productivity tools with a clean, minimalist interface. GreenHub Launcher replaces your default home screen while providing integrated tools for everyday tasks.

## Features

### Core Launcher
- **Custom Home Screen**: Clean, green-themed interface with app shortcuts and widgets
- **App Drawer**: Organized app listing with search functionality
- **Wallpaper Support**: Set wallpapers directly from the launcher
- **Shortcut Management**: Create and manage app shortcuts

### Integrated Tools

#### File Manager
- Browse files and folders on internal storage
- View images, videos, documents
- Copy, move, delete files
- Full storage access for Android 11+

#### Web Browser
- Built-in web browsing
- Network state monitoring
- Internet connectivity check

#### Calculator
- Basic arithmetic operations
- Clean, easy-to-use interface

#### Notepad
- Create and edit text notes
- Save notes locally
- Simple text editor with essential features

#### Music Player
- Play local audio files
- Integrated music visualizer with equalizer
- Background playback service
- Record audio permission for visualizer effects
- Audio settings modification

#### Phone & Contacts
- Make phone calls directly
- Full contacts management
- Read phone state for call handling
- Add, edit, and delete contacts

#### Video Downloader
- Download videos from supported sources
- Download without notification option
- Install downloaded APKs

### Permissions

The app requires the following permissions for full functionality:

| Permission | Purpose |
|:-----------|:--------|
| `SET_WALLPAPER` | Set home screen wallpaper |
| `INSTALL_SHORTCUT` | Create app shortcuts |
| `READ/WRITE_EXTERNAL_STORAGE` | File manager and downloads |
| `MANAGE_EXTERNAL_STORAGE` | Full storage access (Android 11+) |
| `READ_MEDIA_*` | Access media files |
| `CALL_PHONE` | Make phone calls |
| `READ_PHONE_STATE` | Phone call handling |
| `READ/WRITE_CONTACTS` | Contacts management |
| `INTERNET` / `ACCESS_NETWORK_STATE` | Web browser functionality |
| `DOWNLOAD_WITHOUT_NOTIFICATION` | Silent downloads |
| `REQUEST_INSTALL_PACKAGES` | Install APKs |
| `RECORD_AUDIO` | Music visualizer |
| `MODIFY_AUDIO_SETTINGS` | Audio control |

## Tech Stack

- **Language**: Java
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Build Tools**: Gradle 8.2
- **AndroidX**: Enabled
- **Architecture**: Single-module Android app

## Building

### Requirements
- Java 17
- Android SDK 34

### Build Commands

#### Debug APK
```bash
./gradlew assembleDebug --no-daemon
```

#### Release APK
```bash
./gradlew assembleRelease --no-daemon
```

Output APKs will be in: `app/build/outputs/apk/`

### GitHub Actions

The repository includes a GitHub Actions workflow (`.github/workflows/build.yml`) that automatically builds the debug APK on every push to the main branch.

## CI Status

[![Build](https://github.com/miahsobuj/greenhublauncher/actions/workflows/build.yml/badge.svg)](https://github.com/miahsobuj/greenhublauncher/actions)

## Installation

1. Download the latest APK from [Releases](../../releases)
2. Enable "Install from unknown sources" in Settings > Security
3. Install the APK
4. When prompted, set GreenHub Launcher as your default home screen

## Screenshots (To Be Added)

## License

[MIT License](LICENSE)

## Author

- [miahsobuj](https://github.com/miahsobuj)
