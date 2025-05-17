package ru.bmstu.domain.exceptions

sealed class LibraryException : Exception() {
    class LoadError(val itemType: String? = null) : LibraryException()
    class SaveError(val itemType: String? = null) : LibraryException()
    class DeleteError(val itemId: Int) : LibraryException()
    class UpdateError(override val message: String? = null) : LibraryException()
    class NetworkError(override val message: String? = null) : LibraryException()
}