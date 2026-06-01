---
id: getting-started
title: Getting Started
sidebar_label: Getting Started
---

# Getting Started

This guide walks through installing Browser Limit, completing the setup wizard, and configuring the app for your use case.

## Requirements

| Requirement | Details |
|---|---|
| **Android Version** | 8.0 or higher (API 26+) |
| **Shizuku** | Must be installed and running (for uninstallation) |
| **Internet** | Required only for Gemini AI detection (optional) |
| **Storage** | ~15 MB free space |

## Installation

### From GitHub Releases

1. Download the latest APK from the [Releases page](https://github.com/MRAKS05/Browser-Limit/releases).
2. Open the APK file on your device. If prompted, enable installation from unknown sources.
3. Tap **Install** and wait for the installation to complete.

### Building from Source

```bash
git clone https://github.com/MRAKS05/Browser-Limit.git
cd Browser-Limit
./gradlew assembleDebug
```

The debug APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

## Onboarding Wizard

The first time you launch Browser Limit, a 6-step onboarding wizard guides you through the required setup:

### Step 1 -- Welcome

Introduction to Browser Limit and its capabilities. Tap **Continue** to proceed.

### Step 2 -- Notifications

Request notification permission. Required for the foreground service to display its persistent notification and for removal notifications.

### Step 3 -- Display Over Other Apps

Opens the system overlay permission screen. Required for the confirmation dialog that appears when a browser is detected (overlay mode).

### Step 4 -- Shizuku Setup

Requests Shizuku API permission. Shizuku must already be installed and running on your device. See [Shizuku Integration](shizuku.md) for setup instructions.

### Step 5 -- Gemini API Key

Enter your Gemini API key to enable AI-based browser detection. You can:

- **Get a key** -- Tap the button to open Google AI Studio and generate a free API key.
- **Skip** -- Tap "Do it later" to continue without Gemini. Detection will use the local browser database only.

### Step 6 -- Battery Optimization

Request exemption from battery optimization. This ensures the background service is not killed by the system, which would stop monitoring for new app installations.

After completing all steps, you are taken to the Dashboard.

## Configuration

All settings are accessible from the **Settings** tab. The most important options are described below.

### Detection Mode

Choose how Browser Limit responds when a browser is detected:

| Mode | Behavior |
|---|---|
| **Overlay** | Shows a full-screen overlay with a countdown timer. You can tap "Remove Now" or "Keep & Add Exception". If no action is taken, the browser is removed when the timer expires. |
| **Auto-Remove** | Silently uninstalls the browser immediately. A notification is posted with the removal details. |

Only one mode can be active at a time. Enabling one disables the other.

:::tip
Overlay mode is recommended for most users. It gives you a chance to review the detection before removal.
:::

### Countdown Duration

When overlay mode is active, this controls how many seconds the confirmation dialog waits before auto-removing. Range: 5 to 30 seconds.

### Gemini AI

- **Use Gemini AI detection** -- Enable or disable the Gemini API for classifying unknown apps. When disabled, only the local `KNOWN_BROWSERS` database is used.
- **Gemini API Key** -- Your Google AI Studio API key. Enter it here or during onboarding.

### System Chrome Removal

- **Remove Chrome (even if system app)** -- When enabled, Browser Limit will attempt to uninstall Chrome even if it is a pre-installed system app. Disabled by default because system app removal can cause issues on some devices.

### Run on Startup

- **Run on device startup** -- When enabled, the GuardService starts automatically when the device boots. This ensures monitoring begins immediately without manual intervention.

### Animation

- **Show animation** -- Enable or disable the splash screen animation on launch.

## Verifying Your Setup

After completing onboarding, verify everything is working:

1. **Check the Dashboard** -- The System Status card shows whether Shizuku is running and whether Gemini is ready.
2. **Test Shizuku** -- Go to Settings and tap "Test Shizuku Connection". You should see "Shizuku is running!".
3. **Test Gemini** -- Go to Settings and tap "Test Gemini Connection". This sends a test query for Chrome and should return "YES".
4. **Install a known browser** -- Install any browser (e.g., Firefox) to verify the detection and removal flow works.

:::note
If Shizuku is not running, Browser Limit cannot uninstall apps. The detection engine still classifies apps, but removal will fail. See [Troubleshooting](troubleshooting.md) for help.
:::

## Next Steps

- Learn how the [Detection System](detection.md) works.
- Understand [Gemini AI](gemini.md) integration and limits.
- Set up [Shizuku](shizuku.md) if you haven't already.
- Configure [Parental Lock](security.md) for restricted environments.
