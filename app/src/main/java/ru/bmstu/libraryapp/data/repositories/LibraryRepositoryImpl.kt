package ru.bmstu.libraryapp.data.repositories
import android.util.Log
import ru.bmstu.libraryapp.data.datasources.LocalDataSource
import ru.bmstu.libraryapp.domain.entities.BaseLibraryItem
import ru.bmstu.libraryapp.domain.entities.LibraryItem
import ru.bmstu.libraryapp.domain.entities.LibraryItemType
import ru.bmstu.libraryapp.domain.repositories.LibraryRepository
import ru.bmstu.libraryapp.presentation.utils.filterByType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import ru.bmstu.libraryapp.data.pagination.PaginationHelper
import ru.bmstu.libraryapp.data.preferences.LibraryPreferences
import ru.bmstu.libraryapp.domain.exceptions.LibraryException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * Реализация репозитория библиотеки.
 * Работает с источником данных для получения и обновления элементов.
 * @param dataSource Источник данных
 */
class LibraryRepositoryImpl(
    private val dataSource: LocalDataSource,
    preferences: LibraryPreferences
) : LibraryRepository {

    private val bookPagination = PaginationHelper(dataSource, preferences, LibraryItemType.Book::class,  "books")
    private val newspaperPagination = PaginationHelper(dataSource, preferences, LibraryItemType.Newspaper::class, "newspapers")
    private val diskPagination = PaginationHelper(dataSource, preferences, LibraryItemType.Disk::class, "disks")

    /**
     * Получение всех книг.
     *
     * @return Список всех книг
     */

    override suspend fun getAllBooks(): Result<List<LibraryItemType.Book>> = withContext(Dispatchers.IO) {
        bookPagination.handlePaginationRequest(isInitialLoad = true) {
            bookPagination.loadInitial()
        }
    }

    /**
     * Получение всех газет.
     * @return Список всех газет
     */
    override suspend fun getAllNewspapers(): Result<List<LibraryItemType.Newspaper>> = withContext(Dispatchers.IO) {
        newspaperPagination.handlePaginationRequest(isInitialLoad = true) {
            newspaperPagination.loadInitial()
        }
    }

    /**
     * Получение всех дисков.
     * @return Список всех дисков
     */
    override suspend fun getAllDisks(): Result<List<LibraryItemType.Disk>> = withContext(Dispatchers.IO) {
        diskPagination.handlePaginationRequest(isInitialLoad = true) {
            diskPagination.loadInitial()
        }
    }

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

    override suspend fun loadMoreBooks(forward: Boolean): Result<List<LibraryItemType.Book>> = withContext(Dispatchers.IO) {
        if (bookPagination.isLoading) return@withContext Result.success(emptyList())

        bookPagination.isLoading = true
        try {
            bookPagination.handlePaginationRequest(isInitialLoad = false) {
                bookPagination.loadMore(forward)
            }
        } finally {
            bookPagination.isLoading = false
        }
    }

    override suspend fun loadMoreNewspapers(forward: Boolean): Result<List<LibraryItemType.Newspaper>> = withContext(Dispatchers.IO) {
        if (bookPagination.isLoading) return@withContext Result.success(emptyList())

        newspaperPagination.isLoading = true
        try {
            newspaperPagination.handlePaginationRequest(isInitialLoad = false) {
                newspaperPagination.loadMore(forward)
            }
        } finally {
            newspaperPagination.isLoading = false
        }
    }

    override suspend fun loadMoreDisks(forward: Boolean): Result<List<LibraryItemType.Disk>> = withContext(Dispatchers.IO) {
        if (bookPagination.isLoading) return@withContext Result.success(emptyList())

        diskPagination.isLoading = true
        try {
            diskPagination.handlePaginationRequest(isInitialLoad = false) {
                diskPagination.loadMore(forward)
            }
        } finally {
            diskPagination.isLoading = false
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