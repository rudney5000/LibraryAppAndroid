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
    fun getAllBooks(): List<LibraryItemType.Book>

    /**
     * Получение всех газет из репозитория.
     * @return Список всех газет
     */
    fun getAllNewspapers(): List<LibraryItemType.Newspaper>

    /**
     * Получение всех дисков из репозитория.
     * @return Список всех дисков
     */
    fun getAllDisks(): List<LibraryItemType.Disk>

    /**
     * Обновление доступности элемента библиотеки.
     * @param item Элемент для обновления
     * @param isAvailable Новое состояние доступности
     */
    fun updateItemAvailability(item: LibraryItem, isAvailable: Boolean)

    /**
     * Удаление элемента библиотеки по ID.
     * @param itemId ID элемента для удаления
     * @return true если элемент был успешно удален, false в противном случае
     */
    fun deleteItem(itemId: Int): Boolean

    fun addBook(book: LibraryItemType.Book)
    fun addNewspaper(newspaper: LibraryItemType.Newspaper)
    fun addDisk(disk: LibraryItemType.Disk)
    fun updateBook(book: LibraryItemType.Book)
    fun updateNewspaper(newspaper: LibraryItemType.Newspaper)
    fun updateDisk(disk: LibraryItemType.Disk)
}