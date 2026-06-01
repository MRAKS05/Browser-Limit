---
id: troubleshooting
title: Troubleshooting
sidebar_label: Troubleshooting
---

# Troubleshooting

This page covers common issues and their solutions when using Browser Limit.

## Service Issues

### GuardService is not running

**Symptoms:**
- Dashboard shows "Browser Limit is paused"
- New app installations are not detected

**Causes:**
- Service was killed by the system
- "Run on device startup" is disabled
- Battery optimization is active

**Solutions:**

1. Open Browser Limit and toggle the active switch ON.
2. Enable "Run on device startup" in Settings.
3. Disable battery optimization:
   - Go to Settings > Apps > Browser Limit > Battery.
   - Select "Unrestricted".
   - Or tap "Disable Optimization" on the Dashboard.

### Service notification keeps disappearing

**Symptoms:**
- The foreground service notification is not visible

**Causes:**
- Low importance notification channel
- System notification settings

**Solutions:**

1. Go to Settings > Apps > Browser Limit > Notifications.
2. Ensure the "Browser Limit Service" channel is enabled.
3. Set the importance to "Low" or higher.

## Shizuku Issues

### Shizuku is not running

**Symptoms:**
- Dashboard shows "Shizuku: Not Running"

**Solutions:**

1. Open the Shizuku app.
2. Tap **Start via Wireless Debugging** (Android 11+) or use ADB.
3. If using Wireless Debugging, ensure it is enabled in Developer Options.

### Shizuku permission denied

**Symptoms:**
- Dashboard shows "Shizuku Permission Missing"

**Solutions:**

1. Open the Shizuku app.
2. Go to **App List** or **Unauthorized Apps**.
3. Find Browser Limit and grant permission.
4. Or tap "Fix Shizuku" on the Dashboard.

### Uninstallation fails

**Symptoms:**
- Log entry shows "Error" instead of "Removed"
- Notification says "Uninstall Fail"

**Causes:**
- Shizuku service died during uninstallation
- Package name is incorrect
- System app cannot be removed

**Solutions:**

1. Restart Shizuku.
2. Recheck the app from the Logs screen.
3. For system apps, enable "Remove Chrome (even if system app)" if applicable.
4. Some OEM ROMs may restrict Shizuku operations. Try restarting the device.

## Detection Issues

### Browsers are not being detected

**Symptoms:**
- Browser is installed but log shows "Kept"

**Causes:**
- Browser Limit is inactive
- App is in the exceptions list
- App is classified as a system app
- Not in KNOWN_BROWSERS and Gemini is disabled

**Solutions:**

1. Check if Browser Limit is active (Dashboard toggle).
2. Check the Exceptions screen for the app.
3. Check the Logs screen for the detection method and reason.
4. Enable Gemini AI in Settings to classify unknown apps.
5. Use "Recheck with Gemini" in the Logs screen to force reclassification.

### False positives (non-browser apps removed)

**Symptoms:**
- An app that is not a browser is removed

**Causes:**
- App is in the KNOWN_BROWSERS database
- Gemini misclassified the app

**Solutions:**

1. Add the app to the exceptions list.
2. In the Logs screen, tap the app and use "Recheck with Gemini".
3. If Gemini misclassified, the recheck will correct the cache.

### Detection is delayed

**Symptoms:**
- Browser is installed but not detected for several minutes

**Causes:**
- GuardService was killed and restarted
- Battery optimization interrupted the service

**Solutions:**

1. Disable battery optimization for Browser Limit.
2. Enable "Run on device startup".
3. Check the Dashboard for the service status.

## Overlay Issues

### Overlay does not appear

**Symptoms:**
- Browser is installed but no confirmation dialog appears

**Causes:**
- `SYSTEM_ALERT_WINDOW` permission not granted
- Overlay mode is disabled (auto-remove is active)

**Solutions:**

1. Check the Dashboard for "Overlay Permission Missing".
2. Tap "Fix Overlay" to open system settings.
3. Enable "Allow display over other apps" for Browser Limit.
4. Ensure overlay mode is enabled in Settings.

### Overlay disappears immediately

**Symptoms:**
- Overlay appears briefly then closes

**Causes:**
- Another app took focus
- OverlayActivity was killed by the system

**Solutions:**

1. The browser may have been auto-removed. Check the Logs screen.
2. Disable battery optimization for Browser Limit.

## Gemini Issues

### Gemini API key is invalid

**Symptoms:**
- Test Gemini Connection shows "HTTP 401 Unauthorized"

**Solutions:**

1. Go to Settings and verify your API key.
2. Visit [Google AI Studio](https://aistudio.google.com/app/apikey) to generate a new key.
3. Ensure the key has Gemini API access enabled.

### Gemini daily limit reached

**Symptoms:**
- Dashboard shows "Gemini API: Limit Reached"

**Solutions:**

1. Wait until the next day (limit resets at midnight).
2. The local KNOWN_BROWSERS database still provides detection for known browsers.
3. Use "Recheck with Gemini" in the Logs screen to force reclassification (bypasses the limit).

### Gemini returns unexpected results

**Symptoms:**
- Log shows "Fallback (Error: ...)" as the detection method

**Causes:**
- Gemini returned a response that could not be parsed as YES or NO
- Network error during API call

**Solutions:**

1. The fallback to the local database is working correctly.
2. Check your internet connection.
3. Test the Gemini connection from Settings.

## Lock Screen Issues

### Cannot unlock the app

**Symptoms:**
- PIN is not accepted

**Causes:**
- Incorrect PIN
- Waiting mode timer has not expired

**Solutions:**

1. If waiting mode is enabled, wait for the countdown to complete.
2. Ensure you are entering the correct PIN.
3. If you have forgotten your PIN, clear the app's data or reinstall.

### Lock screen re-engages too quickly

**Symptoms:**
- Must re-enter PIN every time you switch apps

**Behavior:**
- This is by design. The lock screen re-engages when the app goes to background (ON_STOP lifecycle event).
- This ensures settings are protected even if you switch away briefly.

## Export Issues

### Log export fails

**Symptoms:**
- Toast shows "Export failed"

**Causes:**
- Storage permission not granted
- Downloads folder not accessible

**Solutions:**

1. Grant storage permission to Browser Limit.
2. The app will automatically fall back to the app's external files directory.
3. Check the Logs screen for the export path.

### Export file is empty

**Symptoms:**
- Exported file has no content

**Causes:**
- No log entries exist
- Logs were cleared

**Solutions:**

1. Check the Logs screen for existing entries.
2. If logs were cleared, they cannot be recovered.

## General

### App crashes on startup

**Solutions:**

1. Clear the app's data: Settings > Apps > Browser Limit > Clear Data.
2. Reinstall the app.
3. Check for Android system updates.

### App uses too much battery

**Causes:**
- Shizuku permission polling every 2 seconds

**Solutions:**
- This is a known behavior. The Dashboard polls Shizuku status every 2 seconds while in the foreground.
- The polling stops when the app is in the background.
- Battery impact is minimal when the app is not in the foreground.

### How do I completely reset Browser Limit?

1. Go to Settings > Apps > Browser Limit.
2. Tap **Clear Data** and **Clear Cache**.
3. Reinstall the app if needed.
4. Complete the onboarding wizard again.

:::warning
Clearing data removes all settings, logs, and exceptions. This action cannot be undone.
:::
