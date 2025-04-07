package ru.bmstu.libraryapp.domain.repositories
import ru.bmstu.libraryapp.domain.entities.Book
import ru.bmstu.libraryapp.domain.entities.Disk
import ru.bmstu.libraryapp.domain.entities.LibraryItem
import ru.bmstu.libraryapp.domain.entities.Newspaper

/**
 * Интерфейс репозитория для работы с элементами библиотеки.
 * Определяет методы получения и обновления элементов.
 */
interface LibraryRepository {

    /**
     * Получение всех книг из репозитория.
     * @return Список всех книг
     */
    fun getAllBooks(): List<Book>

    /**
     * Получение всех газет из репозитория.
     * @return Список всех газет
     */
    fun getAllNewspapers(): List<Newspaper>

    /**
     * Получение всех дисков из репозитория.
     * @return Список всех дисков
     */
    fun getAllDisks(): List<Disk>

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

    fun addBook(book: Book)
    fun addNewspaper(newspaper: Newspaper)
    fun addDisk(disk: Disk)
    fun updateBook(book: Book)
    fun updateNewspaper(newspaper: Newspaper)
    fun updateDisk(disk: Disk)
}