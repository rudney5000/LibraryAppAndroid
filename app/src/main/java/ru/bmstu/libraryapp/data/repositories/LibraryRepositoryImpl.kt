package ru.bmstu.libraryapp.data.repositories
import android.util.Log
import ru.bmstu.libraryapp.data.datasources.LocalDataSource
import ru.bmstu.libraryapp.domain.entities.BaseLibraryItem
import ru.bmstu.libraryapp.domain.entities.LibraryItem
import ru.bmstu.libraryapp.domain.entities.LibraryItemType
import ru.bmstu.libraryapp.domain.repositories.LibraryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import ru.bmstu.libraryapp.data.pagination.PaginationHelper
import ru.bmstu.libraryapp.data.preferences.LibraryPreferences
import ru.bmstu.libraryapp.domain.exceptions.LibraryException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.random.Random

/**
 * Реализация репозитория библиотеки.
 * Работает с источником данных для получения и обновления элементов.
 * @param dataSource Источник данных
 */
class LibraryRepositoryImpl(
    override val dataSource: LocalDataSource,
    preferences: LibraryPreferences
) : LibraryRepository {

    private val paginationHelper = PaginationHelper<LibraryItemType>(
        dataSource,
        preferences,
        LibraryItemType::class,
        "items"
    )

    /**
     * Обновление доступности элемента.
     * @param item Элемент для обновления
     * @param isAvailable Новое состояние доступности
     */
    override suspend fun updateItemAvailability(item: LibraryItem, isAvailable: Boolean): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                simulateDelay()
                if (shouldThrowError()) {
                    Result.failure(LibraryException.UpdateError("Error updating availability"))
                } else {
                    if (item is BaseLibraryItem) {
                        item.changeAvailability(isAvailable)
                        Result.success(Unit)
                    } else {
                        Result.failure(LibraryException.UpdateError("Item type not supported"))
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun deleteItem(itemId: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            simulateDelay()
            if (shouldThrowError()) {
                Result.failure(LibraryException.DeleteError(itemId))
            } else {
                Result.success(dataSource.deleteItem(itemId))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(LibraryException.DeleteError(itemId))
        }
    }

    override suspend fun addBook(book: LibraryItemType.Book): Result<Unit> = withContext(Dispatchers.IO) {
        handleMutation("add book") {
            dataSource.addItem(book)
        }
    }

    override suspend fun addNewspaper(newspaper: LibraryItemType.Newspaper): Result<Unit> = withContext(Dispatchers.IO) {
        handleMutation("add newspaper") {
            dataSource.addItem(newspaper)
        }
    }

    override suspend fun addDisk(disk: LibraryItemType.Disk): Result<Unit> = withContext(Dispatchers.IO) {
        handleMutation("add disk") {
            dataSource.addItem(disk)
        }
    }

    override suspend fun updateBook(book: LibraryItemType.Book): Result<Unit> = withContext(Dispatchers.IO) {
        handleMutation("update book") {
            dataSource.updateItem(book) || throw LibraryException.SaveError("book")
        }
    }

    override suspend fun updateNewspaper(newspaper: LibraryItemType.Newspaper): Result<Unit> = withContext(Dispatchers.IO) {
        handleMutation("update newspaper") {
            dataSource.updateItem(newspaper) || throw LibraryException.SaveError("newspaper")
        }
    }

    override suspend fun updateDisk(disk: LibraryItemType.Disk): Result<Unit> = withContext(Dispatchers.IO) {
        handleMutation("update disk") {
            dataSource.updateItem(disk) || throw LibraryException.SaveError("disk")
        }
    }

    override suspend fun loadMoreItems(forward: Boolean): Result<List<LibraryItemType>> = withContext(Dispatchers.IO) {
        paginationHelper.handlePaginationRequest(isInitialLoad = false) {
            paginationHelper.loadMore(forward).items
        }
    }

    override suspend fun getAllItems(): Result<List<LibraryItemType>> = withContext(Dispatchers.IO) {
        paginationHelper.handlePaginationRequest(isInitialLoad = true) {
            paginationHelper.loadInitial().items
        }
    }
    private suspend fun simulateDelay() = delay(Random.nextLong(100, 300))

    private fun shouldThrowError(): Boolean = Random.nextFloat() < 0.1f

    private suspend fun handleMutation(
        operation: String,
        block: suspend () -> Boolean
    ): Result<Unit> {
        return try {
            simulateDelay()
            if (shouldThrowError()) {
                Result.failure(LibraryException.SaveError(operation))
            } else {
                if (block()) {
                    Result.success(Unit)
                } else {
                    Result.failure(LibraryException.SaveError(operation))
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(LibraryException.SaveError(operation))
        }
    }
}