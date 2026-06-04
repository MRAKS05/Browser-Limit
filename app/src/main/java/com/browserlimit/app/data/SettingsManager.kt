package com.browserlimit.app.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("browserlimit_settings", Context.MODE_PRIVATE)

    private val _isActive = MutableStateFlow(prefs.getBoolean("is_active", true))
    val isActive = _isActive.asStateFlow()

    private val _useGemini = MutableStateFlow(prefs.getBoolean("use_gemini", true))
    val useGemini = _useGemini.asStateFlow()

    private val _showOverlay = MutableStateFlow(prefs.getBoolean("show_overlay", true))
    val showOverlay = _showOverlay.asStateFlow()

    private val _autoRemove = MutableStateFlow(prefs.getBoolean("auto_remove", false))
    val autoRemove = _autoRemove.asStateFlow()

    init {
        ensureOneModeEnabled()
    }

    private fun ensureOneModeEnabled() {
        if (!_showOverlay.value && !_autoRemove.value) {
            prefs.edit().putBoolean("show_overlay", true).putBoolean("auto_remove", false).apply()
            _showOverlay.value = true
            _autoRemove.value = false
        }
    }

    private fun saveMode(showOverlay: Boolean, autoRemove: Boolean) {
        prefs.edit()
            .putBoolean("show_overlay", showOverlay)
            .putBoolean("auto_remove", autoRemove)
            .apply()
        _showOverlay.value = showOverlay
        _autoRemove.value = autoRemove
    }

    private val _countdownDuration = MutableStateFlow(prefs.getInt("countdown_duration", 10))
    val countdownDuration = _countdownDuration.asStateFlow()

    private val _runOnStartup = MutableStateFlow(prefs.getBoolean("run_on_startup", true))
    val runOnStartup = _runOnStartup.asStateFlow()

    private val _showAnimation = MutableStateFlow(prefs.getBoolean("show_animation", true))
    val showAnimation = _showAnimation.asStateFlow()

    private val _geminiApiKey = MutableStateFlow(prefs.getString("gemini_api_key", "") ?: "")
    val geminiApiKey = _geminiApiKey.asStateFlow()

    private val _removeSystemChrome = MutableStateFlow(prefs.getBoolean("remove_system_chrome", false))
    val removeSystemChrome = _removeSystemChrome.asStateFlow()

    private val _parentalLockWaitTime = MutableStateFlow(prefs.getInt("parental_lock_wait_time", 0))
    val parentalLockWaitTime = _parentalLockWaitTime.asStateFlow()

    private val _waitingModeEnabled = MutableStateFlow(prefs.getBoolean("waiting_mode_enabled", false))
    val waitingModeEnabled = _waitingModeEnabled.asStateFlow()

    var hasCompletedSetup: Boolean
        get() = prefs.getBoolean("has_completed_setup", false)
        set(value) {
            prefs.edit().putBoolean("has_completed_setup", value).apply()
        }

    var isParentalLockEnabled: Boolean
        get() = prefs.getBoolean("parental_lock", false)
        set(value) {
            prefs.edit().putBoolean("parental_lock", value).apply()
        }

    var parentalPinHash: String
        get() = prefs.getString("parental_pin_hash", "") ?: ""
        set(value) {
            prefs.edit().putString("parental_pin_hash", value).apply()
        }
        
    var geminiApiCount: Int
        get() = prefs.getInt("gemini_api_count", 0)
        private set(value) {
            prefs.edit().putInt("gemini_api_count", value).apply()
        }
        
    var lastApiDate: String
        get() = prefs.getString("last_api_date", "") ?: ""
        private set(value) {
            prefs.edit().putString("last_api_date", value).apply()
        }

    fun canUseGeminiApi(currentDateStr: String): Boolean {
        if (lastApiDate != currentDateStr) {
            lastApiDate = currentDateStr
            geminiApiCount = 0
        }
        return geminiApiCount < 20
    }

    fun incrementGeminiApiCount() {
        geminiApiCount += 1
    }

    fun setActive(active: Boolean) {
        prefs.edit().putBoolean("is_active", active).apply()
        _isActive.value = active
    }

    fun setUseGemini(use: Boolean) {
        prefs.edit().putBoolean("use_gemini", use).apply()
        _useGemini.value = use
    }

    fun setShowOverlay(show: Boolean) {
        saveMode(showOverlay = show, autoRemove = !show)
    }

    fun setAutoRemove(auto: Boolean) {
        saveMode(showOverlay = !auto, autoRemove = auto)
    }

    fun setCountdownDuration(duration: Int) {
        prefs.edit().putInt("countdown_duration", duration).apply()
        _countdownDuration.value = duration
    }

    fun setRunOnStartup(run: Boolean) {
        prefs.edit().putBoolean("run_on_startup", run).apply()
        _runOnStartup.value = run
    }

    fun setShowAnimation(show: Boolean) {
        prefs.edit().putBoolean("show_animation", show).apply()
        _showAnimation.value = show
    }

    fun setGeminiApiKey(key: String) {
        prefs.edit().putString("gemini_api_key", key).apply()
        _geminiApiKey.value = key
    }

    fun setRemoveSystemChrome(remove: Boolean) {
        prefs.edit().putBoolean("remove_system_chrome", remove).apply()
        _removeSystemChrome.value = remove
    }

    fun setParentalLockWaitTime(time: Int) {
        prefs.edit().putInt("parental_lock_wait_time", time).apply()
        _parentalLockWaitTime.value = time
    }

    fun setWaitingModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("waiting_mode_enabled", enabled).apply()
        _waitingModeEnabled.value = enabled
    }

    var geminiConfirmedBrowsers: Set<String>
        get() = prefs.getStringSet("gemini_confirmed_browsers", emptySet()) ?: emptySet()
        set(value) {
            prefs.edit().putStringSet("gemini_confirmed_browsers", value).apply()
        }

    fun addConfirmedBrowser(packageName: String) {
        val current = geminiConfirmedBrowsers.toMutableSet()
        current.add(packageName)
        geminiConfirmedBrowsers = current
    }

    var geminiConfirmedNonBrowsers: Set<String>
        get() = prefs.getStringSet("gemini_confirmed_non_browsers", emptySet()) ?: emptySet()
        set(value) {
            prefs.edit().putStringSet("gemini_confirmed_non_browsers", value).apply()
        }

    fun addConfirmedNonBrowser(packageName: String) {
        val current = geminiConfirmedNonBrowsers.toMutableSet()
        current.add(packageName)
        geminiConfirmedNonBrowsers = current
    }

    fun removeConfirmedCache(packageName: String) {
        val browsers = geminiConfirmedBrowsers.toMutableSet()
        browsers.remove(packageName)
        geminiConfirmedBrowsers = browsers
        
        val nonBrowsers = geminiConfirmedNonBrowsers.toMutableSet()
        nonBrowsers.remove(packageName)
        geminiConfirmedNonBrowsers = nonBrowsers
    }
}
