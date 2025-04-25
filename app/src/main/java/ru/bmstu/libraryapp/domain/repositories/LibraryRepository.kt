package ru.bmstu.libraryapp.domain.repositories
import ru.bmstu.libraryapp.domain.entities.LibraryItem
import ru.bmstu.libraryapp.domain.entities.LibraryItemType

/**
 * Интерфейс репозитория для работы с элементами библиотеки.
 * Определяет методы получения и обновления элементов.
 */
interface LibraryRepository {

    /**
     * Получение всех книг из репозитория.
     * @return Список всех книг
     */
    suspend fun getAllBooks(): Result<List<LibraryItemType.Book>>

    /**
     * Получение всех газет из репозитория.
     * @return Список всех газет
     */
    suspend fun getAllNewspapers(): Result<List<LibraryItemType.Newspaper>>

    /**
     * Получение всех дисков из репозитория.
     * @return Список всех дисков
     */
    suspend fun getAllDisks(): Result<List<LibraryItemType.Disk>>

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
    fun setSortOrder(sortOrder: String)
    fun getSortOrder(): String
}