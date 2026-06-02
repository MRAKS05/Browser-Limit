---
id: parental-control
title: Parental Control and Security
sidebar_label: Parental Control
---

# Security and Parental Lock

Browser Limit includes a parental lock system designed to prevent unauthorized or impulsive changes to the app's configuration. This is particularly useful in environments where the app is used for digital wellness or parental control.

## How It Works

When parental lock is enabled, all configuration changes and certain actions require PIN verification. The system also supports a waiting mode that enforces a delay before changes can be made.

## Enabling Parental Lock

1. Go to **Settings** > **Security Settings**.
2. Expand the Security Settings section.
3. Toggle **Enable Parental Lock** to ON.
4. Enter a numeric PIN when prompted.
5. Tap **Confirm**.

:::warning
The PIN is stored as a plaintext string in SharedPreferences. This is a known limitation ([#6](https://github.com/MRAKS05/Browser-Limit/issues/6)). In a production environment, the PIN should be hashed (e.g., using SHA-256 or bcrypt) before storage.
:::

## What Is Locked

When parental lock is active, the following actions require PIN verification:

| Action | Screen |
|---|---|
| Toggle Browser Limit on/off | Dashboard |
| Change Gemini AI settings | Settings |
| Change detection mode (overlay/auto-remove) | Settings |
| Change countdown duration | Settings |
| Change system Chrome removal setting | Settings |
| Add or remove exceptions | Exceptions |
| Change startup behavior | Settings |

:::note
The Logs screen is not locked. Users can view, recheck, and export logs without entering the PIN.
:::

## Lock Screen Behavior

When you open Browser Limit with parental lock enabled, you see the lock screen:

1. The app shows "App Locked" with a lock icon.
2. If waiting mode is enabled, a countdown timer is displayed.
3. After the wait time expires (or immediately if waiting mode is disabled), a PIN input field appears.
4. Enter the correct PIN and tap **Unlock**.

### App Lifecycle

The lock screen re-engages when:
- The app is moved to the background (ON_STOP lifecycle event).
- The app loses window focus.

This means every time you switch away from Browser Limit and return, you must re-enter the PIN.

## Waiting Mode

Waiting mode adds a mandatory delay before PIN entry is allowed. This prevents impulsive changes by forcing you to wait before modifying settings.

### Configuration

| Setting | Range | Default |
|---|---|---|
| Waiting Mode | On/Off | Off |
| Wait Time | 10-300 seconds | 10 seconds |

### How It Works

1. When a PIN-protected action is triggered, the `ParentalUnlockDialog` appears.
2. If waiting mode is enabled, a countdown timer starts.
3. During the countdown, the PIN input is not shown. A message reads: "To prevent impulsive changes, please wait. Do not leave the app or the timer will reset."
4. After the timer expires, the PIN input field becomes available.
5. Enter the correct PIN and tap **Confirm**.

### Timer Reset

The waiting mode timer resets when:
- The app loses window focus (user switches to another app).
- The user leaves the Browser Limit app.

This ensures the waiting period must be completed in a single, uninterrupted session.

## ParentalUnlockDialog

The unlock dialog is a reusable component (`ParentalUnlockDialog.kt`) used across multiple screens. It is triggered whenever a locked action is attempted:

```kotlin
fun executeAction(action: () -> Unit) {
    if (settings.isParentalLockEnabled) {
        pendingAction = action
    } else {
        action()
    }
}
```

The dialog shows:

- **Without waiting mode**: PIN input field and Confirm/Cancel buttons.
- **With waiting mode**: Countdown timer with progress indicator, then PIN input.

## Disabling Parental Lock

To disable parental lock:

1. Go to **Settings** > **Security Settings**.
2. Toggle **Enable Parental Lock** to OFF.
3. Enter your PIN when prompted.

:::note
Disabling parental lock also disables auto-remove mode and re-enables overlay mode. This is a safety measure to prevent silent removals without user confirmation.
:::

## Security Considerations

### Current Limitations

- **PIN storage**: The PIN is stored as plaintext in SharedPreferences. It is not hashed or encrypted.
- **No brute-force protection**: There is no lockout after multiple failed PIN attempts.
- **No PIN recovery**: If you forget your PIN, you must clear the app's data or reinstall.

### Recommendations

For production use, the following improvements are recommended:

1. **Hash the PIN** using SHA-256 or bcrypt before storing.
2. **Add a brute-force limit** (e.g., lock out after 5 failed attempts).
3. **Implement PIN recovery** via a backup code or email.
4. **Encrypt SharedPreferences** using AndroidX Security library.
