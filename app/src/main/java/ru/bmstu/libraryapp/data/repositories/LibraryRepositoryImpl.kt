package ru.bmstu.libraryapp.data.repositories
import ru.bmstu.libraryapp.data.datasources.LocalDataSource
import ru.bmstu.libraryapp.domain.entities.BaseLibraryItem
import ru.bmstu.libraryapp.domain.entities.LibraryItem
import ru.bmstu.libraryapp.domain.entities.LibraryItemType
import ru.bmstu.libraryapp.domain.repositories.LibraryRepository
import ru.bmstu.libraryapp.presentation.utils.filterByType

/**
 * Реализация репозитория библиотеки.
 * Работает с источником данных для получения и обновления элементов.
 * @param dataSource Источник данных
 */
class LibraryRepositoryImpl(private val dataSource: LocalDataSource) : LibraryRepository {
    /**
     * Получение всех книг.
     * @return Список всех книг
     */
    override fun getAllBooks(): List<LibraryItemType.Book> {
        return dataSource.getAllItems().filterByType<LibraryItemType.Book>()
    }

    /**
     * Получение всех газет.
     * @return Список всех газет
     */
    override fun getAllNewspapers(): List<LibraryItemType.Newspaper> {
        return dataSource.getAllItems().filterByType<LibraryItemType.Newspaper>()
    }


    /**
     * Получение всех дисков.
     * @return Список всех дисков
     */
    override fun getAllDisks(): List<LibraryItemType.Disk> {
        return dataSource.getAllItems().filterByType<LibraryItemType.Disk>()
    }

    /**
     * Обновление доступности элемента.
     * @param item Элемент для обновления
     * @param isAvailable Новое состояние доступности
     */
    override fun updateItemAvailability(item: LibraryItem, isAvailable: Boolean) {
        if (item is BaseLibraryItem) {
            item.changeAvailability(isAvailable)
        }
    }

    override fun deleteItem(itemId: Int): Boolean = dataSource.deleteItem(itemId)


    override fun addBook(book: LibraryItemType.Book) {
        dataSource.addItem(book)
    }

    override fun addNewspaper(newspaper: LibraryItemType.Newspaper) {
        dataSource.addItem(newspaper)
    }

    override fun addDisk(disk: LibraryItemType.Disk) {
        dataSource.addItem(disk)
    }

    override fun updateBook(book: LibraryItemType.Book) {
        dataSource.updateItem(book)
    }

    override fun updateNewspaper(newspaper: LibraryItemType.Newspaper) {
        dataSource.updateItem(newspaper)
    }

    override fun updateDisk(disk: LibraryItemType.Disk) {
        dataSource.updateItem(disk)
    }
}