package ru.bmstu.libraryapp.domain.repositories

import ru.bmstu.libraryapp.domain.entities.LibraryItemType

interface GoogleBooksRepository {
    suspend fun searchBooks(author: String?, title: String?): Result<List<LibraryItemType.Book>>
}