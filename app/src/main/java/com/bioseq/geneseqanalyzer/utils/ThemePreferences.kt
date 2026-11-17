package com.bioseq.geneseqanalyzer.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

object ThemePreferences {

    private const val PREF_NAME = "geneseq_theme_prefs"
    const val KEY_THEME_MODE = "key_theme_mode"

    const val MODE_SYSTEM = 0
    const val MODE_LIGHT = 1
    const val MODE_DARK = 2

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getSavedThemeMode(context: Context): Int {
        return getPreferences(context).getInt(KEY_THEME_MODE, MODE_SYSTEM)
    }

    fun saveThemeMode(context: Context, mode: Int) {
        getPreferences(context).edit().putInt(KEY_THEME_MODE, mode).apply()
        applyTheme(mode)
    }

    fun applySavedTheme(context: Context) {
        val mode = getSavedThemeMode(context)
        applyTheme(mode)
    }

    private fun applyTheme(mode: Int) {
        val nightMode = when (mode) {
            MODE_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            MODE_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
}