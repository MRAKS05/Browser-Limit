# Code Reference / API Overview

## Main Classes & Flows

### `MainActivity`
- Handles setup and lock screen logic.
- Kicks off content and GuardService if active.

### `GuardService`
- Watches for app install events and enforces removals.
- Foreground service with user notification.
- Uses Shizuku for rootless uninstall.

### `BootReceiver`
- Listens for phone restart, starts enforcement automatically if configured.

### `SettingsManager`
- Interface for all persistent and configuration state, including AI usage and PIN lock.

---

For details, check the [source code on GitHub](https://github.com/MRAKS05/Browser-Limit).
