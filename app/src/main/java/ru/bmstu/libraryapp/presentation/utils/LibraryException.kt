package ru.bmstu.libraryapp.presentation.utils

sealed class LibraryException : Exception() {
    class LoadError(val itemType: String) : LibraryException()
    class SaveError(val itemType: String) : LibraryException()
    class DeleteError(val itemId: Int) : LibraryException()
    class UpdateError(val itemType: String) : LibraryException()
}