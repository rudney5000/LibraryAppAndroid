package ru.bmstu.libraryapp.domain.repositories
import ru.bmstu.libraryapp.data.datasources.LocalDataSource
import ru.bmstu.libraryapp.domain.entities.LibraryItem
import ru.bmstu.libraryapp.domain.entities.LibraryItemType

/**
 * Интерфейс репозитория для работы с элементами библиотеки.
 * Определяет методы получения и обновления элементов.
 */
interface LibraryRepository {
    /**
     * Обновление доступности элемента библиотеки.
     * @param item Элемент для обновления
     * @param isAvailable Новое состояние доступности
     */
    suspend fun updateItemAvailability(item: LibraryItem, isAvailable: Boolean): Result<Unit>

    /**
     * Удаление элемента библиотеки по ID.
     * @param itemId ID элемента для удаления
     * @return true если элемент был успешно удален, false в противном случае
     */
    suspend fun deleteItem(itemId: Int): Result<Boolean>
    suspend fun addBook(book: LibraryItemType.Book): Result<Unit>
    suspend fun addNewspaper(newspaper: LibraryItemType.Newspaper): Result<Unit>
    suspend fun addDisk(disk: LibraryItemType.Disk): Result<Unit>
    suspend fun updateBook(book: LibraryItemType.Book): Result<Unit>
    suspend fun updateNewspaper(newspaper: LibraryItemType.Newspaper): Result<Unit>
    suspend fun updateDisk(disk: LibraryItemType.Disk): Result<Unit>
    suspend fun loadMoreItems(forward: Boolean): Result<List<LibraryItemType>>
    suspend fun getAllItems(): Result<List<LibraryItemType>>
    val dataSource: LocalDataSource
}