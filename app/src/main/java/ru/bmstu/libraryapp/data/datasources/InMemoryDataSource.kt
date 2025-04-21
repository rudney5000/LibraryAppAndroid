package ru.bmstu.libraryapp.data.datasources
import ru.bmstu.libraryapp.domain.entities.DiskType
import ru.bmstu.libraryapp.domain.entities.LibraryItem
import ru.bmstu.libraryapp.domain.entities.LibraryItemType
import ru.bmstu.libraryapp.domain.entities.Month

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
    )

    override fun getAllItems(): List<LibraryItemType> {
        return items.toList()
    }

    override fun deleteItem(id: Int): Boolean {
        val index = items.indexOfFirst { it.id == id }
        return if (index != -1) {
            items.removeAt(index)
            true
        } else false
    }

    override fun addItem(item: LibraryItem): Boolean {
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

    override fun updateItem(item: LibraryItem): Boolean {
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