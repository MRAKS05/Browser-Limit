# System Architecture & Infrastructure

- **Language**: Kotlin (majority), some Python scripts, Jetpack Compose UI
- **Core Modules**:
  - `MainActivity` — main UI/root activity and navigation
  - `GuardService` — background enforcement, install blocking
  - `BootReceiver` — autostart support
  - `SettingsManager` — local persistent & secure settings
- **Datastore**: Room (local, no cloud sync)
- **API usage**: Shizuku for rootless uninstall, Gemini for AI browser detection (if enabled)

## Android Manifest Permissions

- Foreground service
- Request install packages
- System alert window (overlay)
- Usage access, query all packages

See [`AndroidManifest.xml`](../app/src/main/AndroidManifest.xml) for details.
