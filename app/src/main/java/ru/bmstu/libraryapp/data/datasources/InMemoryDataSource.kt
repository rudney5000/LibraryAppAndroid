package ru.bmstu.libraryapp.data.datasources
import ru.bmstu.libraryapp.domain.entities.BaseLibraryItem
import ru.bmstu.libraryapp.domain.entities.Book
import ru.bmstu.libraryapp.domain.entities.DigitizableItem
import ru.bmstu.libraryapp.domain.entities.Disk
import ru.bmstu.libraryapp.domain.entities.DiskType
import ru.bmstu.libraryapp.domain.entities.LibraryItem
import ru.bmstu.libraryapp.domain.entities.Month
import ru.bmstu.libraryapp.domain.entities.Newspaper
import ru.bmstu.libraryapp.domain.entities.ParcelableLibraryItem

/**
 * Реализация источника данных в памяти.
 * Содержит предварительно заполненные списки книг, газет и дисков.
 */
class InMemoryDataSource private constructor() : LocalDataSource {
    /** Список книг в памяти */
    private val books = mutableListOf(
        Book(1001, "Маугли", true, 202, "Джозеф Киплинг"),
        Book(1002, "Война и мир", true, 1225, "Лев Толстой"),
        Book(1003, "Преступление и наказание", false, 672, "Федор Достоевский"),
        Book(1004, "Мастер и Маргарита", true, 448, "Михаил Булгаков")
    )

    /** Список газет в памяти */
    private val newspapers = mutableListOf(
        Newspaper(2001, "Сельская жизнь", true, 794, Month.MARCH),
        Newspaper(2002, "Аргументы и факты", false, 123, Month.APRIL),
        Newspaper(2003, "Коммерсантъ", true, 456, Month.JANUARY),
        Newspaper(2004, "Известия", true, 789, Month.OCTOBER)
    )

    /** Список дисков в памяти */
    private val disks = mutableListOf(
        Disk(3001, "Дэдпул и Росомаха", true, DiskType.DVD),
        Disk(3002, "Лучшие песни 2023", false, DiskType.CD),
        Disk(3003, "Звездные войны: Эпизод IX", true, DiskType.DVD),
        Disk(3004, "Классическая музыка", true, DiskType.CD)
    )

    override fun getAllItems(): List<LibraryItem> {
        val allItems = mutableListOf<LibraryItem>()
        allItems.addAll(books)
        allItems.addAll(newspapers)
        allItems.addAll(disks)
        return allItems
    }

    override fun deleteItem(itemId: Int): Boolean {
        books.removeIf { it.id == itemId }
        newspapers.removeIf { it.id == itemId }
        disks.removeIf { it.id == itemId }
        return true
    }

    override fun addItem(item: LibraryItem): Boolean {
        val newId = generateNewId(item)
        when (item) {
            is Book -> books.add(0, item.copy(id = newId))
            is Newspaper -> newspapers.add(0, item.copy(id = newId))
            is Disk -> disks.add(0, item.copy(id = newId))
            is BaseLibraryItem -> TODO()
            is ParcelableLibraryItem -> TODO()
            is DigitizableItem -> TODO()
        }
        return true
    }

    override fun updateItem(item: LibraryItem): Boolean {
        when (item) {
            is Book -> {
                val index = books.indexOfFirst { it.id == item.id }
                if (index != -1) {
                    books[index] = item
                    return true
                }
            }
            is Newspaper -> {
                val index = newspapers.indexOfFirst { it.id == item.id }
                if (index != -1) {
                    newspapers[index] = item
                    return true
                }
            }
            is Disk -> {
                val index = disks.indexOfFirst { it.id == item.id }
                if (index != -1) {
                    disks[index] = item
                    return true
                }
            }
            is BaseLibraryItem -> TODO()
            is ParcelableLibraryItem -> TODO()
            is DigitizableItem -> TODO()
        }
        return false
    }

    private fun generateNewId(item: LibraryItem): Int {
        val prefix = when (item) {
            is Book -> 1000
            is Newspaper -> 2000
            is Disk -> 3000
            is BaseLibraryItem -> TODO()
            is ParcelableLibraryItem -> TODO()
            is DigitizableItem -> TODO()
        }
        val existingIds = when (item) {
            is Book -> books.map { it.id }
            is Newspaper -> newspapers.map { it.id }
            is Disk -> disks.map { it.id }
            is BaseLibraryItem -> TODO()
            is ParcelableLibraryItem -> TODO()
            is DigitizableItem -> TODO()
        }
        return if (existingIds.isEmpty()) prefix + 1
        else existingIds.maxOrNull()!! + 1
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