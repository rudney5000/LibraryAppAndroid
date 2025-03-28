package ru.bmstu.libraryapp.data.datasources
import ru.bmstu.libraryapp.domain.entities.Book
import ru.bmstu.libraryapp.domain.entities.Disk
import ru.bmstu.libraryapp.domain.entities.DiskType
import ru.bmstu.libraryapp.domain.entities.LibraryItem
import ru.bmstu.libraryapp.domain.entities.Month
import ru.bmstu.libraryapp.domain.entities.Newspaper

/**
     * Реализация источника данных в памяти.
     * Содержит предварительно заполненные списки книг, газет и дисков.
     */
    class InMemoryDataSource : LocalDataSource {

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
    }