package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("dolphin_settings", Context.MODE_PRIVATE)

    private val _companyName = MutableStateFlow(getCompanyName())
    val companyName: StateFlow<String> = _companyName

    private val _currency = MutableStateFlow(getCurrency())
    val currency: StateFlow<String> = _currency

    private val _isDarkMode = MutableStateFlow(isDarkMode())
    val isDarkMode: StateFlow<Boolean> = _isDarkMode

    private val _notificationTime = MutableStateFlow(getNotificationTime())
    val notificationTime: StateFlow<String> = _notificationTime

    private val _isAppLockEnabled = MutableStateFlow(isAppLockEnabled())
    val isAppLockEnabled: StateFlow<Boolean> = _isAppLockEnabled

    private val _isBiometricEnabled = MutableStateFlow(isBiometricEnabled())
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled

    fun getCompanyName(): String = prefs.getString("company_name", "Dolphin Fiber Network") ?: "Dolphin Fiber Network"
    fun setCompanyName(name: String) {
        prefs.edit().putString("company_name", name).apply()
        _companyName.value = name
    }

    fun getCurrency(): String = prefs.getString("currency", "PKR") ?: "PKR"
    fun setCurrency(curr: String) {
        prefs.edit().putString("currency", curr).apply()
        _currency.value = curr
    }

    fun isDarkMode(): Boolean = prefs.getBoolean("dark_mode", false)
    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean("dark_mode", enabled).apply()
        _isDarkMode.value = enabled
    }

    fun getNotificationTime(): String = prefs.getString("notification_time", "09:00") ?: "09:00"
    fun setNotificationTime(time: String) {
        prefs.edit().putString("notification_time", time).apply()
        _notificationTime.value = time
    }

    fun isAppLockEnabled(): Boolean = prefs.getBoolean("app_lock", false)
    fun setAppLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("app_lock", enabled).apply()
        _isAppLockEnabled.value = enabled
    }

    fun getAppPin(): String = prefs.getString("app_pin", "") ?: ""
    fun setAppPin(pin: String) {
        prefs.edit().putString("app_pin", pin).apply()
    }

    fun isBiometricEnabled(): Boolean = prefs.getBoolean("biometric_enabled", false)
    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("biometric_enabled", enabled).apply()
        _isBiometricEnabled.value = enabled
    }
}
