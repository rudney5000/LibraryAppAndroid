package ru.bmstu.domain.usecases

import ru.bmstu.domain.models.LibraryItemType
import ru.bmstu.domain.repositories.LibraryRepository

class AddDiskUseCase(private val repository: LibraryRepository) {
    suspend operator fun invoke(disk: LibraryItemType.Disk): Result<Unit> {
        return repository.addDisk(disk)
    }
}