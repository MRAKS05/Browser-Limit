---
id: faq
title: FAQ
sidebar_label: FAQ
---

# Frequently Asked Questions

## General

### What is Browser Limit?

Browser Limit is an Android application that automatically detects and removes web browsers from your device. It uses a two-tier detection system: a local database of known browser package names, and Google Gemini AI for semantic classification of unknown apps.

### What Android versions are supported?

Android 8.0 (API 26) and above. The minimum SDK is 26 and the target SDK is 36.

### Is root required?

No. Browser Limit uses the Shizuku API for rootless package management. Shizuku provides ADB-level permissions without requiring device root. See [Shizuku Integration](shizuku.md) for setup details.

### Is this available on the Play Store?

No. Browser Limit is distributed via [GitHub Releases](https://github.com/MRAKS05/Browser-Limit/releases). The `QUERY_ALL_PACKAGES` permission is restricted on the Play Store, which is why the app is distributed as a direct APK.

### What does the license allow?

Browser Limit is licensed under CC BY-NC 4.0 (Creative Commons Attribution-NonCommercial). You can share and adapt the software for non-commercial purposes with appropriate attribution.

## Detection

### How does Browser Limit detect browsers?

The detection engine checks installed apps through multiple tiers:

1. **Exceptions list** -- If the app is in the exceptions list, it is skipped.
2. **System app check** -- System apps are skipped (unless it is Chrome with system Chrome removal enabled).
3. **Local cache** -- Previously classified apps are resolved from cache.
4. **Known browser database** -- A hardcoded list of 50+ browser package names.
5. **Gemini AI** -- For unknown apps, Gemini analyzes Play Store metadata to classify the app.

See [Detection System](detection.md) for the full flow.

### Can I allow a specific browser?

Yes. Add the browser's package name to the exceptions list in the Exceptions screen. See [Exception Management](exceptions.md).

### How do I find an app's package name?

- Check the Logs screen after the app has been scanned.
- Use `adb shell pm list packages` on your device.
- Look at the app's Play Store URL: the package name appears after `id=`.

### Does it detect VPN apps or proxy browsers?

Gemini AI classifies apps based on their primary purpose. Apps whose primary purpose is proxying or unblocking websites are classified as browsers. Apps that merely use a WebView for specific content (e.g., social media apps) are not flagged.

### What happens if Gemini is disabled?

Detection falls back to the local `KNOWN_BROWSERS` database only. Apps in the database are removed; all other apps are allowed. This provides reliable detection for major browsers but cannot detect new or obscure browsers.

## Gemini AI

### Do I need a Gemini API key?

No. The API key is optional. Without it, Browser Limit uses only the local browser database. With it, unknown apps are also classified by Gemini AI.

### How do I get a Gemini API key?

1. Visit [Google AI Studio](https://aistudio.google.com/app/apikey).
2. Sign in with your Google account.
3. Create a project and generate an API key.
4. Paste the key into Browser Limit Settings.

### Is there a usage limit?

Browser Limit enforces a self-imposed limit of 20 Gemini API requests per day. This resets automatically at midnight. The actual Gemini API has much higher quotas for personal use.

### What if Gemini returns an error?

The engine falls back to the local `KNOWN_BROWSERS` database. Common errors and their causes:

| Error | Cause |
|---|---|
| HTTP 429 | Daily limit reached (self-imposed or API quota) |
| HTTP 401 | Invalid API key |
| HTTP 403 | API key does not have Gemini access |
| Timeout | Network issue or Gemini service slow |
| Connection refused | No internet connection |

### Does Gemini send data to the cloud?

Yes. When Gemini is enabled, the app's package name and Play Store metadata are sent to Google's Gemini API for classification. No personal data, device information, or other app data is sent. When Gemini is disabled, no data leaves the device.

## Shizuku

### What is Shizuku?

Shizuku is an Android app that provides ADB-level permissions to other apps without root. Browser Limit uses it to execute `pm uninstall` commands for rootless app removal.

### Do I need to keep Shizuku running?

Yes. Shizuku must be running for Browser Limit to uninstall apps. If Shizuku stops, detection still works but removal fails. See [Shizuku Integration](shizuku.md).

### Which Shizuku start method should I use?

Wireless Debugging is recommended for most users (requires Android 11+). ADB via computer is a reliable alternative if wireless debugging is not available.

### Can I use Browser Limit without Shizuku?

You can install and use the app, but uninstallation will fail. The detection and logging features still work, but detected browsers will not be removed.

## Security

### Is the parental lock secure?

The parental lock provides a basic level of protection against impulsive changes. The PIN prevents unauthorized configuration changes and exception modifications. However, the PIN is stored in plaintext in SharedPreferences, which is a known limitation.

### Can I recover a forgotten PIN?

There is no built-in PIN recovery mechanism. If you forget your PIN, you can clear the app's data (Settings > Apps > Browser Limit > Clear Data) or reinstall the app. This will reset all settings and logs.

### Does Browser Limit collect personal data?

No. Browser Limit does not collect, transmit, or store any personal data beyond what is described in this documentation. All data is stored locally on your device. The only external communication is with the Gemini API (when enabled) and the Play Store (for metadata scraping).

## Troubleshooting

### The service keeps stopping

This is usually caused by battery optimization. Disable battery optimization for Browser Limit:

1. Go to Settings > Apps > Browser Limit > Battery.
2. Select "Unrestricted".

Or use the "Disable Optimization" button on the Dashboard.

### Browsers are not being removed

Check the following:

1. Is Shizuku running? (Dashboard shows "Shizuku: Running")
2. Is Browser Limit active? (Toggle is ON)
3. Is the app in the exceptions list? (Exceptions screen)
4. Check the Logs screen for the detection result.

### The overlay does not appear

Ensure `SYSTEM_ALERT_WINDOW` permission is granted. Check the Dashboard for missing permissions.

### Gemini keeps returning errors

1. Verify your API key in Settings.
2. Check your internet connection.
3. Ensure the Gemini API is enabled in your Google Cloud project.
4. The daily limit (20/day) may be reached. Wait until the next day.
