package ru.bmstu.domain.usecases

import ru.bmstu.domain.repositories.LibraryRepository

class DeleteItemUseCase(private val repository: LibraryRepository) {
    suspend operator fun invoke(itemId: Int): Result<Boolean> {
        return repository.deleteItem(itemId)
    }
}