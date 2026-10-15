package com.example.util

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig

object KeyPreferencesHelper {
    private const val PREFS_NAME = "avanavis_security_prefs"
    private const val KEY_GEMINI_API = "gemini_api_key_persistent"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getStoredApiKey(context: Context): String {
        return getPrefs(context).getString(KEY_GEMINI_API, "")?.trim() ?: ""
    }

    fun setStoredApiKey(context: Context, key: String) {
        getPrefs(context).edit().putString(KEY_GEMINI_API, key.trim()).apply()
    }

    fun clearStoredApiKey(context: Context) {
        getPrefs(context).edit().remove(KEY_GEMINI_API).apply()
    }

    fun getEffectiveApiKey(context: Context, runtimeOverride: String? = null): String {
        if (!runtimeOverride.isNullOrBlank()) {
            return runtimeOverride.trim()
        }
        val storedKey = getStoredApiKey(context)
        if (storedKey.isNotBlank()) {
            return storedKey
        }
        return try {
            val buildKey = BuildConfig.GEMINI_API_KEY
            if (buildKey.isNotBlank() && !buildKey.contains("MY_GEMINI_API_KEY")) {
                buildKey.trim()
            } else {
                ""
            }
        } catch (e: Throwable) {
            ""
        }
    }

    fun isKeyAvailable(context: Context, runtimeOverride: String? = null): Boolean {
        val key = getEffectiveApiKey(context, runtimeOverride)
        return key.isNotBlank() && !key.contains("MY_GEMINI_API_KEY")
    }

    fun getMaskedKey(key: String): String {
        val trimmed = key.trim()
        if (trimmed.isBlank() || trimmed.contains("MY_GEMINI_API_KEY")) {
            return "تنظیم نشده"
        }
        return if (trimmed.length > 8) {
            "${trimmed.take(6)}...${trimmed.takeLast(4)}"
        } else {
            "********"
        }
    }

    fun getKeySourceDescription(context: Context): String {
        val storedKey = getStoredApiKey(context)
        if (storedKey.isNotBlank()) {
            return "ذخیره‌شده به صورت دائمی در حافظه دستگاه"
        }
        return try {
            val buildKey = BuildConfig.GEMINI_API_KEY
            if (buildKey.isNotBlank() && !buildKey.contains("MY_GEMINI_API_KEY")) {
                "بارگذاری‌شده از Secrets پنل هوش مصنوعی (BuildConfig)"
            } else {
                "هنوز کلید API تنظیم نشده است"
            }
        } catch (e: Throwable) {
            "هنوز کلید API تنظیم نشده است"
        }
    }
}
