---
id: shizuku
title: Shizuku Integration
sidebar_label: Shizuku
---

# Shizuku Integration

Browser Limit uses the Shizuku API to perform rootless package uninstallation. Shizuku provides ADB-level permissions without requiring device root, making it the ideal solution for automated app removal.

## What Is Shizuku

Shizuku is an Android app that provides a way for other apps to use system APIs with ADB (Android Debug Bridge) permissions. It runs a service that grants elevated privileges to authorized apps, enabling operations like package uninstallation without root access.

Browser Limit uses Shizuku to execute the `pm uninstall` command, which removes apps for the current user without modifying the system partition.

## Setup Guide

### 1. Install Shizuku

Download and install the Shizuku app from one of these sources:

- [Google Play Store](https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api)
- [GitHub Releases](https://github.com/RikkaApps/Shizuku/releases)

### 2. Start Shizuku

Shizuku offers two methods to start its service:

**Wireless Debugging (Recommended)**

1. Enable **Developer Options** on your device (tap Build Number 7 times in Settings > About Phone).
2. Enable **Wireless Debugging** in Developer Options.
3. Open Shizuku and tap **Start via Wireless Debugging**.
4. Pair with the device using the pairing code shown in Developer Options.
5. Shizuku will start and show "Shizuku is running".

**ADB via Computer**

1. Connect your device to a computer with ADB installed.
2. Run: `adb shell sh /storage/emulated/0/Android/data/moe.shizuku.privileged.api/start.sh`
3. Shizuku will start and show "Shizuku is running".

:::tip
Wireless Debugging is the easiest method and does not require a computer. However, it requires Android 11 or higher.
:::

### 3. Grant Permission to Browser Limit

1. Open Shizuku.
2. Go to **App List** or **Unauthorized Apps**.
3. Find **Browser Limit** and grant permission.

Alternatively, Browser Limit will prompt for Shizuku permission during onboarding. Tap **Fix Shizuku** on the Dashboard if permission has not been granted.

## How Browser Limit Uses Shizuku

### Uninstallation Command

When Browser Limit needs to remove an app, it calls:

```kotlin
ShizukuHelper.uninstall(packageName)
```

This executes the equivalent of:

```bash
pm uninstall -k --user 0 <package_name>
```

| Flag | Meaning |
|---|---|
| `-k` | Keep the app's data and cache directories |
| `--user 0` | Uninstall for the current user only (does not affect other users on multi-user devices) |

This approach:
- Does **not** require root access.
- Removes the app from the current user's app drawer and launcher.
- Preserves the app's data (can be restored if the app is reinstalled).
- Does **not** modify the system partition (safe for all devices).

### Permission Check

Before attempting uninstallation, Browser Limit checks:

```kotlin
if (!Shizuku.pingBinder()) {
    // Shizuku is not running
    return false
}
```

If Shizuku is not running or has not granted permission, the uninstallation fails gracefully. The detection result is still logged, but the app remains installed.

## Shizuku Provider

Browser Limit registers the Shizuku provider in `AndroidManifest.xml`:

```xml
<provider
    android:name="rikka.shizuku.ShizukuProvider"
    android:authorities="${applicationId}.shizuku"
    android:exported="true"
    android:multiprocess="false"
    android:enabled="true"
    android:permission="android.permission.INTERACT_ACROSS_USERS_FULL" />
```

This allows the Shizuku manager to communicate with Browser Limit and grant elevated permissions.

## Dependencies

Browser Limit includes two Shizuku libraries:

```kotlin
implementation("dev.rikka.shizuku:api:13.1.5")
implementation("dev.rikka.shizuku:provider:13.1.5")
```

- **api** -- Provides the Shizuku API for calling system commands.
- **provider** -- Registers the content provider for Shizuku communication.

## Troubleshooting

| Issue | Cause | Fix |
|---|---|---|
| "Shizuku is not running" | Shizuku service stopped | Open Shizuku app and restart the service |
| "Shizuku permission denied" | Permission not granted | Open Shizuku > App List > Grant to Browser Limit |
| Uninstall fails silently | Shizuku binder died | Restart Shizuku, then recheck from Logs screen |
| "Shizuku is NOT Running" on Dashboard | Service not started | Start Shizuku via Wireless Debugging or ADB |

See [Troubleshooting](troubleshooting.md) for more details.
