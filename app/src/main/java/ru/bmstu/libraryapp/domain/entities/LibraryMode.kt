package ru.bmstu.libraryapp.domain.entities

sealed class LibraryMode {
    object Local : LibraryMode()
    object GoogleBooks : LibraryMode()
}