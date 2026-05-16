package com.umg.muebleria.localization

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object AppLocale {
    private const val PREFS = "muebleria_app_prefs"
    private const val KEY_LANG = "language_tag"

    fun applyPersistedLocale(context: Context) {
        val tag = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_LANG, null)
            ?: return
        if (tag.isBlank()) return
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
    }

    fun persistAndApply(context: Context, languageTag: String?) {
        val app = context.applicationContext
        val prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
        if (languageTag.isNullOrBlank()) {
            prefs.remove(KEY_LANG).apply()
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
        } else {
            prefs.putString(KEY_LANG, languageTag).apply()
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageTag))
        }
    }
}
