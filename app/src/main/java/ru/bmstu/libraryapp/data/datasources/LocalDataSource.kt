package ru.bmstu.libraryapp.data.datasources
import ru.bmstu.libraryapp.domain.entities.LibraryItem

/**
 * Интерфейс источника данных для элементов библиотеки.
 */
interface LocalDataSource {
    /**
     * Получение всех элементов библиотеки из источника данных.
     * @return Список всех элементов библиотеки
     */
    fun getAllItems(): List<LibraryItem>

    fun deleteItem(itemId: Int): Boolean

    fun addItem(item: LibraryItem): Boolean
    fun updateItem(item: LibraryItem): Boolean
}