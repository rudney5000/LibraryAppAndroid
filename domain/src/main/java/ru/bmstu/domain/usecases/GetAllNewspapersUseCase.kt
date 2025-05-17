package ru.bmstu.domain.usecases

import ru.bmstu.domain.models.LibraryItemType
import ru.bmstu.domain.repositories.LibraryRepository

class GetAllNewspapersUseCase(private val repository: LibraryRepository) {
    suspend operator fun invoke(): Result<List<LibraryItemType.Newspaper>> {
        return repository.getAllNewspapers()
    }
}