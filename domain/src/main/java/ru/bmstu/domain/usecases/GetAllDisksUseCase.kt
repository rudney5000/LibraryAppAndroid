package ru.bmstu.domain.usecases

import ru.bmstu.domain.models.LibraryItemType
import ru.bmstu.domain.repositories.LibraryRepository

class GetAllDisksUseCase(private val repository: LibraryRepository) {
    suspend operator fun invoke(): Result<List<LibraryItemType.Disk>> {
        return repository.getAllDisks()
    }
}