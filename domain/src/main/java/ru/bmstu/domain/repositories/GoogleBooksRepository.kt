package ru.bmstu.domain.repositories

import ru.bmstu.domain.models.LibraryItemType

interface GoogleBooksRepository {
    suspend fun searchBooks(author: String?, title: String?): Result<List<LibraryItemType.Book>>
}