package ru.bmstu.domain.usecases

import ru.bmstu.domain.models.LibraryItemType
import ru.bmstu.domain.repositories.LibraryRepository

class AddNewspaperUseCase(private val repository: LibraryRepository) {
    suspend operator fun invoke(newspaper: LibraryItemType.Newspaper): Result<Unit> {
        return repository.addNewspaper(newspaper)
    }
}