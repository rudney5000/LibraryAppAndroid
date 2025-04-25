package ru.bmstu.libraryapp.domain.exceptions

sealed class LibraryException : Exception() {
    class LoadError(val itemType: String) : LibraryException()
    class SaveError(val itemType: String) : LibraryException()
    class DeleteError(val itemId: Int) : LibraryException()
    class UpdateError(val itemType: String) : LibraryException()
}