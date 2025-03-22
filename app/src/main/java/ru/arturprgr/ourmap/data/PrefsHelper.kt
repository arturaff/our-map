package ru.arturprgr.ourmap.data

import android.content.Context
import androidx.core.content.edit

class PrefsHelper {
    companion object {
        fun set(context: Context, key: String, value: String) =
            context.getSharedPreferences("sPrefs", Context.MODE_PRIVATE).edit {
                putString(key, value).apply()
            }

        fun get(context: Context, key: String): String =
            context.getSharedPreferences("sPrefs", Context.MODE_PRIVATE).getString(key, null)
                .toString()
    }
}