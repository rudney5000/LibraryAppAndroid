package ru.bmstu.domain.usecases

import ru.bmstu.domain.models.LibraryItemType
import ru.bmstu.domain.repositories.LibraryRepository

class UpdateBookUseCase(private val repository: LibraryRepository) {
    suspend operator fun invoke(book: LibraryItemType.Book): Result<Unit> {
        return repository.updateBook(book)
    }
}