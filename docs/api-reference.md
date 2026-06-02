---
id: api-reference
title: API Reference
sidebar_label: API Reference
---

# API Reference

Complete class and schema documentation for the Browser Limit codebase.

## Entry Points

### MainActivity

**File**: `app/src/main/java/com/example/MainActivity.kt`

| Method | Description |
|---|---|
| `onCreate()` | Installs splash screen, starts GuardService, sets up Compose content with lock screen and navigation |

**Composables**:

| Function | Description |
|---|---|
| `LockScreen(settings, onUnlocked)` | PIN entry screen with optional waiting mode countdown |
| `MainScreen()` | Navigation host with bottom bar (Dashboard, Exceptions, Logs, Settings) |

**Sealed class `Screen`**:

| Object | Route | Title |
|---|---|---|
| `Onboarding` | `onboarding` | Setup |
| `Dashboard` | `dashboard` | Dashboard |
| `Exceptions` | `exceptions` | Exceptions |
| `Logs` | `logs` | Logs |
| `Settings` | `settings` | Settings |

---

## Data Layer

### SettingsManager

**File**: `app/src/main/java/com/example/data/SettingsManager.kt`

SharedPreferences file: `browserlimit_settings`

| Property | Type | Default | Description |
|---|---|---|---|
| `isActive` | `StateFlow<Boolean>` | `true` | Whether monitoring is active |
| `useGemini` | `StateFlow<Boolean>` | `true` | Whether Gemini AI is enabled |
| `showOverlay` | `StateFlow<Boolean>` | `true` | Overlay mode enabled |
| `autoRemove` | `StateFlow<Boolean>` | `false` | Auto-remove mode enabled |
| `countdownDuration` | `StateFlow<Int>` | `10` | Overlay countdown in seconds (5-30) |
| `runOnStartup` | `StateFlow<Boolean>` | `true` | Start service on boot |
| `showAnimation` | `StateFlow<Boolean>` | `true` | Show splash animation |
| `geminiApiKey` | `StateFlow<String>` | `""` | Gemini API key |
| `removeSystemChrome` | `StateFlow<Boolean>` | `false` | Remove system Chrome |
| `parentalLockWaitTime` | `StateFlow<Int>` | `0` | Waiting mode duration (10-300s) |
| `waitingModeEnabled` | `StateFlow<Boolean>` | `false` | Waiting mode enabled |
| `hasCompletedSetup` | `Boolean` | `false` | Onboarding completed |
| `isParentalLockEnabled` | `Boolean` | `false` | Parental lock enabled |
| `parentalPinHash` | `String` | `""` | Parental lock PIN (plaintext) |
| `geminiApiCount` | `Int` | `0` | API calls today |
| `lastApiDate` | `String` | `""` | Date of last API call |
| `geminiConfirmedBrowsers` | `Set<String>` | `emptySet()` | Cached browser classifications |
| `geminiConfirmedNonBrowsers` | `Set<String>` | `emptySet()` | Cached non-browser classifications |

| Method | Description |
|---|---|
| `canUseGeminiApi(currentDateStr)` | Check if daily limit (20/day) is not reached; resets counter on new day |
| `incrementGeminiApiCount()` | Increment the daily API counter |
| `setActive(active)` | Toggle monitoring on/off |
| `setUseGemini(use)` | Toggle Gemini AI |
| `setShowOverlay(show)` | Set overlay mode (disables auto-remove) |
| `setAutoRemove(auto)` | Set auto-remove mode (disables overlay) |
| `setCountdownDuration(duration)` | Set countdown timer (5-30s) |
| `setRunOnStartup(run)` | Toggle startup behavior |
| `setShowAnimation(show)` | Toggle splash animation |
| `setGeminiApiKey(key)` | Set Gemini API key |
| `setRemoveSystemChrome(remove)` | Toggle system Chrome removal |
| `setParentalLockWaitTime(time)` | Set waiting mode duration |
| `setWaitingModeEnabled(enabled)` | Toggle waiting mode |
| `addConfirmedBrowser(packageName)` | Cache a browser classification |
| `addConfirmedNonBrowser(packageName)` | Cache a non-browser classification |
| `removeConfirmedCache(packageName)` | Remove a package from both caches |

---

### ExceptionsManager

**File**: `app/src/main/java/com/example/data/ExceptionsManager.kt`

SharedPreferences file: `browserlimit_exceptions`

| Property | Type | Description |
|---|---|---|
| `exceptionsFlow` | `StateFlow<List<String>>` | Observable list of exception package names |

| Method | Description |
|---|---|
| `isExcepted(packageName)` | Check if a package is in exceptions (includes permanent exceptions) |
| `addException(packageName)` | Add a package to the exceptions list |
| `removeException(packageName)` | Remove a package (except permanent exceptions) |
| `setCustomExceptions(packages)` | Bulk-add packages to exceptions |

**Permanent exceptions**: `com.aistudio.browserlimit.abxyz`, `com.example`

---

### LogEntry

**File**: `app/src/main/java/com/example/data/LogEntry.kt`

Room entity for the `logs` table.

| Column | Type | Description |
|---|---|---|
| `id` | `Int` (auto-generated PK) | Unique identifier |
| `timestamp` | `Long` | Unix timestamp in milliseconds |
| `appName` | `String` | App name from PackageManager |
| `packageName` | `String` | Android package name |
| `detectionMethod` | `String` | Classification method used |
| `decision` | `String` | Action taken (Removed/Kept/Excepted/Error) |
| `geminiResponse` | `String` | Detection reason or Gemini response |
| `shizukuResult` | `String` | Shizuku result (reserved) |

---

### LogDao

**File**: `app/src/main/java/com/example/data/LogDao.kt`

| Query | Return Type | Description |
|---|---|---|
| `getAllLogs()` | `Flow<List<LogEntry>>` | All logs, newest first |
| `getLogCount()` | `suspend Int` | Total log count |
| `getRemovedTodayCount(todayStart)` | `Flow<Int>` | Count of removals today |
| `getScannedTodayCount(todayStart)` | `Flow<Int>` | Count of scans today |
| `insertLog(log)` | `suspend` | Insert a log entry |
| `trimLogs()` | `suspend` | Keep only the last 500 entries |
| `clearAll()` | `suspend` | Delete all log entries |

---

### LogDatabase

**File**: `app/src/main/java/com/example/data/LogDatabase.kt`

Room database singleton. Database name: `browserlimit_logs`. Version: 1.

```kotlin
LogDatabase.getDatabase(context).logDao()
```

---

## Engine Layer

### BrowserDetector

**File**: `app/src/main/java/com/example/engine/BrowserDetector.kt`

| Method | Description |
|---|---|
| `checkPackage(packageName, forceRecheck)` | Run the full detection flow and return a `DetectionResult` |

**DetectionResult data class**:

| Field | Type | Description |
|---|---|---|
| `isBrowser` | `Boolean` | Whether the app is classified as a browser |
| `method` | `String` | How it was classified |
| `reason` | `String` | Human-readable explanation |

---

### BrowserDatabase

**File**: `app/src/main/java/com/example/engine/BrowserDatabase.kt`

Singleton object containing the hardcoded array of 50+ known browser package names.

```kotlin
object BrowserDatabase {
    val KNOWN_BROWSERS: Array<String>  // 50+ package names
}
```

---

### GeminiClient

**File**: `app/src/main/java/com/example/engine/GeminiClient.kt`

| Method | Description |
|---|---|
| `isBrowser(packageName)` | Classify an app as browser/non-browser via Gemini API |

**API endpoint**: `POST /v1beta/models/gemini-flash-lite-latest:generateContent`

**Request body** (serialized via kotlinx-serialization):

```kotlin
@Serializable
data class GenerateContentRequest(
    val systemInstruction: Content? = null,
    val contents: List<Content>,
    val tools: List<JsonObject>? = null,
    val generationConfig: GenerationConfig? = null
)
```

---

### ShizukuUninstaller

**File**: `app/src/main/java/com/example/engine/ShizukuUninstaller.kt`

| Method | Description |
|---|---|
| `uninstallPackage(packageName)` | Uninstall an app via Shizuku. Returns `true` on success. |

---

### NotificationHelper

**File**: `app/src/main/java/com/example/engine/NotificationHelper.kt`

| Method | Description |
|---|---|
| `sendUninstallNotification(context, appName, packageName, reason)` | Post a notification about an app removal |

Notification channel: `uninstall_notifications` ("Uninstalled Browsers")

---

## Service Layer

### GuardService

**File**: `app/src/main/java/com/example/service/GuardService.kt`

Foreground service that monitors for new app installations.

| Action | Description |
|---|---|
| Default | Start foreground notification |
| `PAUSE_RESUME` | Toggle `isActive` setting |

Notification channel: `GuardServiceChannel` ("Browser Limit Service")

---

### AppInstallReceiver

**File**: `app/src/main/java/com/example/receiver/AppInstallReceiver.kt`

BroadcastReceiver for `ACTION_PACKAGE_ADDED`. Triggers the full detection and removal flow.

---

### BootReceiver

**File**: `app/src/main/java/com/example/receiver/BootReceiver.kt`

BroadcastReceiver for `BOOT_COMPLETED` and `LOCKED_BOOT_COMPLETED`. Starts GuardService if `runOnStartup` is enabled.

---

## UI Components

### ParentalUnlockDialog

**File**: `app/src/main/java/com/example/ui/components/ParentalUnlockDialog.kt`

Reusable PIN unlock dialog with optional waiting mode countdown.

| Parameter | Type | Description |
|---|---|---|
| `settings` | `SettingsManager` | Settings instance for PIN verification |
| `onSuccess` | `() -> Unit` | Called when correct PIN is entered |
| `onCancel` | `() -> Unit` | Called when dialog is dismissed |

---

## Dependencies

| Library | Version | Purpose |
|---|---|---|
| Shizuku API | 13.1.5 | Rootless package management |
| Lottie Compose | 6.4.0 | Splash screen animation |
| kotlinx-serialization | 1.7.3 | JSON serialization for Gemini API |
| Room | BOM-managed | Local database |
| Retrofit | BOM-managed | HTTP client for Gemini API |
| OkHttp | BOM-managed | HTTP transport |
| Moshi | BOM-managed | JSON parsing |
| Coil | BOM-managed | Image loading |
| Firebase BOM | BOM-managed | Firebase integration (reserved) |
| SplashScreen | 1.0.1 | Android 12+ splash screen |
