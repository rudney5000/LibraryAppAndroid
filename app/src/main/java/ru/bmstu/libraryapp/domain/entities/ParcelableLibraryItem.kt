package ru.bmstu.libraryapp.domain.entities

import android.os.Parcelable

interface ParcelableLibraryItem : LibraryItem, Parcelable {
    val isDigitizable: Boolean
    fun digitize(): String
}