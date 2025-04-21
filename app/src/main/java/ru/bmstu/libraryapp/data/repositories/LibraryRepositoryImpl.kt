package ru.bmstu.libraryapp.data.repositories
import ru.bmstu.libraryapp.data.datasources.LocalDataSource
import ru.bmstu.libraryapp.domain.entities.BaseLibraryItem
import ru.bmstu.libraryapp.domain.entities.LibraryItem
import ru.bmstu.libraryapp.domain.entities.LibraryItemType
import ru.bmstu.libraryapp.domain.repositories.LibraryRepository
import ru.bmstu.libraryapp.presentation.utils.filterByType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * Реализация репозитория библиотеки.
 * Работает с источником данных для получения и обновления элементов.
 * @param dataSource Источник данных
 */
class LibraryRepositoryImpl(private val dataSource: LocalDataSource) : LibraryRepository {

    /**
     * Получение всех книг.
     * @return Список всех книг
     */
    override suspend fun getAllBooks(): Result<List<LibraryItemType.Book>> = withContext(Dispatchers.IO) {
        try {
            simulateDelay()
            if (shouldThrowError()) {
                Result.failure(Exception("Erreur lors du chargement des livres"))
            } else {
                Result.success(dataSource.getAllItems().filterByType())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Получение всех газет.
     * @return Список всех газет
     */

    override suspend fun getAllNewspapers(): Result<List<LibraryItemType.Newspaper>> = withContext(Dispatchers.IO) {
        try {
            simulateDelay()
            if (shouldThrowError()) {
                Result.failure(Exception("Erreur lors du chargement des journaux"))
            } else {
                Result.success(dataSource.getAllItems().filterByType())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    /**
     * Получение всех дисков.
     * @return Список всех дисков
     */

    override suspend fun getAllDisks(): Result<List<LibraryItemType.Disk>> = withContext(Dispatchers.IO) {
        try {
            simulateDelay()
            if (shouldThrowError()) {
                Result.failure(Exception("Erreur lors du chargement des disques"))
            } else {
                Result.success(dataSource.getAllItems().filterByType())
            }
        } catch (e: Exception) {
            Result.failure(e)
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
                    Result.failure(Exception("Erreur lors de la mise à jour de la disponibilité"))
                } else {
                    if (item is BaseLibraryItem) {
                        item.changeAvailability(isAvailable)
                        Result.success(Unit)
                    } else {
                        Result.failure(Exception("Type d'item non supporté"))
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
                Result.failure(Exception("Erreur lors de la suppression"))
            } else {
                Result.success(dataSource.deleteItem(itemId))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addBook(book: LibraryItemType.Book): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            simulateDelay()
            if (shouldThrowError()) {
                Result.failure(Exception("Erreur lors de l'ajout du livre"))
            } else {
                dataSource.addItem(book)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addNewspaper(newspaper: LibraryItemType.Newspaper): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                simulateDelay()
                if (shouldThrowError()) {
                    Result.failure(Exception("Erreur lors de l'ajout du journal"))
                } else {
                    dataSource.addItem(newspaper)
                    Result.success(Unit)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
        override suspend fun addDisk(disk: LibraryItemType.Disk): Result<Unit> = withContext(Dispatchers.IO) {
            try {
                simulateDelay()
                if (shouldThrowError()) {
                    Result.failure(Exception("Erreur lors de l'ajout du disque"))
                } else {
                    dataSource.addItem(disk)
                    Result.success(Unit)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun updateBook(book: LibraryItemType.Book): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            simulateDelay()
            if (shouldThrowError()) {
                Result.failure(Exception("Erreur lors de la mise à jour du livre"))
            } else {
                dataSource.updateItem(book)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateNewspaper(newspaper: LibraryItemType.Newspaper): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                simulateDelay()
                if (shouldThrowError()) {
                    Result.failure(Exception("Erreur lors de la mise à jour du journal"))
                } else {
                    dataSource.updateItem(newspaper)
                    Result.success(Unit)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun updateDisk(disk: LibraryItemType.Disk): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            simulateDelay()
            if (shouldThrowError()) {
                Result.failure(Exception("Erreur lors de la mise à jour du disque"))
            } else {
                dataSource.updateItem(disk)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    private suspend fun simulateDelay() {
        delay(Random.nextLong(100, 2000))
    }

    private fun shouldThrowError(): Boolean {
        return false
    }
}