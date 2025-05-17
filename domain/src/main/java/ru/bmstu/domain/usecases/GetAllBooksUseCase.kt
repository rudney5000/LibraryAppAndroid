package ru.bmstu.domain.usecases

import ru.bmstu.domain.models.LibraryItemType
import ru.bmstu.domain.repositories.LibraryRepository

class GetAllBooksUseCase(private val repository: LibraryRepository) {
    suspend operator fun invoke(): Result<List<LibraryItemType.Book>> {
        return repository.getAllBooks()
    }
}