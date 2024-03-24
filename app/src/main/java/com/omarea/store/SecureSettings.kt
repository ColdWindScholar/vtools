package com.omarea.store

import android.content.Context
import android.provider.Settings

class SecureSettings(context: Context) {
    private val contentResolver = context.contentResolver

    fun getString(key: String): String? {
        return Settings.Secure.getString(contentResolver, key)
    }

    fun setString(key: String, value: String): Boolean {
        return Settings.Secure.putString(contentResolver, key, value)
    }
}