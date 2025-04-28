package ru.bmstu.libraryapp.data.datasources
import ru.bmstu.libraryapp.domain.entities.LibraryItem
import ru.bmstu.libraryapp.domain.entities.LibraryItemType

/**
 * Интерфейс источника данных для элементов библиотеки.
 */
interface LocalDataSource {
    suspend fun getAllItems(
        sortBy: String = "title",
        limit: Int = Int.MAX_VALUE,
        offset: Int = 0
    ): List<LibraryItemType>

    suspend fun deleteItem(itemId: Int): Boolean

    suspend fun addItem(item: LibraryItem): Boolean

    suspend fun updateItem(item: LibraryItem): Boolean

    suspend fun getItemCount(): Int
}