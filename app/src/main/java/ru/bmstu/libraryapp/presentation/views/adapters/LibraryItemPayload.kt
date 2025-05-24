package ru.bmstu.libraryapp.presentation.views.adapters

sealed class LibraryItemPayload {
    data class AvailabilityChanged(val newAvailability: Boolean) : LibraryItemPayload()
}