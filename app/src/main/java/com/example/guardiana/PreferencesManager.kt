package com.example.guardiana

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

class PreferencesManager @SuppressLint("CommitPrefEdits")
constructor(context: Context) {

    private val preferences: SharedPreferences
    private val editor: SharedPreferences.Editor

    init {
        preferences = context.getSharedPreferences(PREFERENCE_CONFIGURATION_NAME, PRIVATE_MODE)
        editor = preferences.edit()
    }

    fun isFirstRun() = preferences.getBoolean(FIRST_TIME, true)

    fun setFirstRun(b: Boolean) {
        editor.putBoolean(FIRST_TIME, b).commit()
        editor.commit()
    }

    companion object {
        private const val PRIVATE_MODE = 0
        private const val PREFERENCE_CONFIGURATION_NAME = "configuration"
        private const val FIRST_TIME = "isFirstRun"
    }
}