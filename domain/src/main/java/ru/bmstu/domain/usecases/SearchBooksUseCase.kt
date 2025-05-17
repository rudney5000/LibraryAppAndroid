package ru.bmstu.domain.usecases

import ru.bmstu.common.types.LibraryMode
import ru.bmstu.domain.models.LibraryItemType
import ru.bmstu.domain.repositories.GoogleBooksRepository
import ru.bmstu.domain.repositories.LibraryRepository

class SearchBooksUseCase(
    private val libraryRepository: LibraryRepository,
    private val googleBooksRepository: GoogleBooksRepository
) {
    suspend operator fun invoke(query: String, mode: LibraryMode = LibraryMode.LOCAL): Result<List<LibraryItemType.Book>> {
        return when (mode) {
            LibraryMode.LOCAL -> {
                libraryRepository.getAllBooks().map { books ->
                    books.filter { book ->
                        book.title.contains(query, ignoreCase = true) ||
                        book.author.contains(query, ignoreCase = true)
                    }
                }
            }
            LibraryMode.GOOGLE_BOOKS -> {
                googleBooksRepository.searchBooks(query, query)
            }
        }
    }
}