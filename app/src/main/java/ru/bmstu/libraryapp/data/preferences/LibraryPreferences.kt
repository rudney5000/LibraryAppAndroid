package ru.bmstu.libraryapp.data.preferences

import android.content.Context

import android.content.SharedPreferences

class LibraryPreferences(context: Context) {
    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var sortOrder: String
        get() = prefs.getString(KEY_SORT_ORDER, DEFAULT_SORT_ORDER) ?: DEFAULT_SORT_ORDER
        set(value) = prefs.edit().putString(KEY_SORT_ORDER, value).apply()

    var pageSize: Int
        get() = prefs.getInt(KEY_PAGE_SIZE, DEFAULT_PAGE_SIZE)
        set(value) = prefs.edit().putInt(KEY_PAGE_SIZE, value).apply()

    companion object {
        private const val PREFS_NAME = "library_preferences"
        private const val KEY_SORT_ORDER = "sort_order"
        private const val KEY_PAGE_SIZE = "page_size"
        private const val DEFAULT_SORT_ORDER = "title"
        private const val DEFAULT_PAGE_SIZE = 90
    }
}