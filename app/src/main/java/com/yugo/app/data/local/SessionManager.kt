package com.yugo.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.yugo.app.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _isLoggedIn = MutableStateFlow(prefs.getBoolean(KEY_IS_LOGGED_IN, false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentUser = MutableStateFlow(loadUser())
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _walletBalance = MutableStateFlow(loadUser()?.yugoCashBalance ?: 500.0)
    val walletBalance: StateFlow<Double> = _walletBalance.asStateFlow()

    companion object {
        private const val PREF_NAME = "yugo_secure_session_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER = "current_user_json"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_BASE_URL = "custom_base_url"

        @Volatile
        private var INSTANCE: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SessionManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private fun loadUser(): User? {
        val json = prefs.getString(KEY_USER, null) ?: return null
        return try {
            gson.fromJson(json, User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun saveAuthSession(accessToken: String, refreshToken: String, user: User) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putString(KEY_USER, gson.toJson(user))
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()

        _isLoggedIn.value = true
        _currentUser.value = user
        _walletBalance.value = user.yugoCashBalance
    }

    fun updateUser(user: User) {
        prefs.edit()
            .putString(KEY_USER, gson.toJson(user))
            .apply()
        _currentUser.value = user
        _walletBalance.value = user.yugoCashBalance
    }

    fun updateWalletBalance(newBalance: Double) {
        val user = _currentUser.value ?: return
        val updated = user.copy(yugoCashBalance = newBalance)
        updateUser(updated)
    }

    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    fun setAccessToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    fun getCustomBaseUrl(): String? {
        return prefs.getString(KEY_BASE_URL, null)
    }

    fun setCustomBaseUrl(url: String?) {
        if (url.isNullOrBlank()) {
            prefs.edit().remove(KEY_BASE_URL).apply()
        } else {
            prefs.edit().putString(KEY_BASE_URL, url.trim()).apply()
        }
    }

    fun clearSession() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_USER)
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .apply()

        _isLoggedIn.value = false
        _currentUser.value = null
        _walletBalance.value = 0.0
    }
}
