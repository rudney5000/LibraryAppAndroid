package ru.bmstu.data.datasources
import ru.bmstu.common.types.LibraryItem
import ru.bmstu.domain.models.LibraryItemType

/**
 * Интерфейс источника данных для элементов библиотеки.
 */
interface LocalDataSource {

    suspend fun getItemsPage(
        page: Int,
        pageSize: Int,
        sortBy: String = "title"
    ): List<LibraryItemType>

    @Deprecated("Use getItemsPage instead")
    suspend fun getAllItems(
        sortBy: String = "title",
        limit: Int = Int.MAX_VALUE,
        offset: Int = 0
    ): List<LibraryItemType> {
        val pageSize = limit
        val page = offset / limit
        return getItemsPage(page, pageSize, sortBy)
    }

    suspend fun deleteItem(itemId: Int): Boolean

    suspend fun addItem(item: LibraryItem): Boolean

    suspend fun updateItem(item: LibraryItem): Boolean

    suspend fun getItemCount(): Int
}