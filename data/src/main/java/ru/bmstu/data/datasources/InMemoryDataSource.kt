package ru.bmstu.data.datasources
import ru.bmstu.common.types.DiskType
import ru.bmstu.common.types.LibraryItem
import ru.bmstu.common.types.Month
import ru.bmstu.domain.models.LibraryItemType

/**
 * Реализация источника данных в памяти.
 * Содержит предварительно заполненные списки книг, газет и дисков.
 */
class InMemoryDataSource private constructor() : LocalDataSource {
    /** Список */
    private val items = mutableListOf(
        LibraryItemType.Book(1001, "Маугли", true, 202, "Джозеф Киплинг"),
        LibraryItemType.Book(1002, "Война и мир", true, 1225, "Лев Толстой"),
        LibraryItemType.Book(1003, "Преступление и наказание", false, 672, "Федор Достоевский"),
        LibraryItemType.Book(1004, "Мастер и Маргарита", true, 448, "Михаил Булгаков"),

        LibraryItemType.Newspaper(2001, "Сельская жизнь", true, 794, Month.MARCH),
        LibraryItemType.Newspaper(2002, "Аргументы и факты", false, 123, Month.APRIL),
        LibraryItemType.Newspaper(2003, "Коммерсантъ", true, 456, Month.JANUARY),
        LibraryItemType.Newspaper(2004, "Известия", true, 789, Month.OCTOBER),

        LibraryItemType.Disk(3001, "Дэдпул и Росомаха", true, DiskType.DVD),
        LibraryItemType.Disk(3002, "Лучшие песни 2023", false, DiskType.CD),
        LibraryItemType.Disk(3003, "Звездные войны: Эпизод IX", true, DiskType.DVD),
        LibraryItemType.Disk(3004, "Классическая музыка", true, DiskType.CD)
    ).apply {
        for (i in 1..50) {
            add(LibraryItemType.Book(1000 + i, "Книга $i", true, 200 + i, "Автор $i"))
            add(LibraryItemType.Newspaper(2000 + i, "Газета $i", true, i, Month.values()[i % 12]))

            val diskTypes = DiskType.values()
            add(LibraryItemType.Disk(3000 + i, "Диск $i", true, diskTypes[i % diskTypes.size]))
        }
    }

    override suspend fun getItemsPage(
        page: Int,
        pageSize: Int,
        sortBy: String
    ): List<LibraryItemType> {
        val sortedItems = when (sortBy.lowercase()) {
            "title" -> items.sortedBy { it.title }
            "date", "createdat" -> items.sortedByDescending {
                when(it) {
                    is LibraryItemType.Book -> it.id
                    is LibraryItemType.Newspaper -> it.id
                    is LibraryItemType.Disk -> it.id
                }
            }
            "author" -> items.sortedBy {
                when(it) {
                    is LibraryItemType.Book -> it.author
                    else -> it.title
                }
            }
            else -> items.sortedBy { it.title }
        }
        val offset = page * pageSize
        if (offset >= sortedItems.size) {
            return emptyList()
        }
        return sortedItems.subList(offset, minOf(offset + pageSize, sortedItems.size))
    }

    @Deprecated("Use getItemsPage instead")
    override suspend fun getAllItems(
        sortBy: String,
        limit: Int,
        offset: Int
    ): List<LibraryItemType> {
        return getItemsPage(offset / limit, limit, sortBy)
    }

    override suspend fun deleteItem(itemId: Int): Boolean {
        val index = items.indexOfFirst { it.id == itemId }
        return if (index != -1) {
            items.removeAt(index)
            true
        } else false
    }

    override suspend fun addItem(item: LibraryItem): Boolean {
        return when (item) {
            is LibraryItemType -> {
                val newId = generateNewId()
                when (item) {
                    is LibraryItemType.Book -> items.add(0, item.copy(id = newId))
                    is LibraryItemType.Newspaper -> items.add(0, item.copy(id = newId))
                    is LibraryItemType.Disk -> items.add(0, item.copy(id = newId))
                }
                true
            }
            else -> false
        }
    }

    override suspend fun updateItem(item: LibraryItem): Boolean {
        return when (item) {
            is LibraryItemType -> {
                val index = items.indexOfFirst { it.id == item.id }
                if (index != -1) {
                    items[index] = item
                    true
                } else false
            }
            else -> false
        }
    }

    override suspend fun getItemCount(): Int {
        return items.size
    }

    private fun generateNewId(): Int {
        return if (items.isEmpty()) 1
        else items.maxOf { it.id } + 1
    }

    companion object {
        @Volatile
        private var instance: InMemoryDataSource? = null

        fun getInstance(): InMemoryDataSource {
            return instance ?: synchronized(this) {
                instance ?: InMemoryDataSource().also { instance = it }
            }
        }
    }
}