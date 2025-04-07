package ru.bmstu.libraryapp.data.repositories
import ru.bmstu.libraryapp.data.datasources.LocalDataSource
import ru.bmstu.libraryapp.domain.entities.BaseLibraryItem
import ru.bmstu.libraryapp.domain.entities.Book
import ru.bmstu.libraryapp.domain.entities.Disk
import ru.bmstu.libraryapp.domain.entities.LibraryItem
import ru.bmstu.libraryapp.domain.entities.Newspaper
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
    override fun getAllBooks(): List<Book> = dataSource.getAllItems().filterByType<Book>()

    /**
     * Получение всех газет.
     * @return Список всех газет
     */
    override fun getAllNewspapers(): List<Newspaper> = dataSource.getAllItems().filterByType<Newspaper>()

    /**
     * Получение всех дисков.
     * @return Список всех дисков
     */
    override fun getAllDisks(): List<Disk> = dataSource.getAllItems().filterByType<Disk>()

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


    override fun addBook(book: Book) {
        dataSource.addItem(book)
    }

    override fun addNewspaper(newspaper: Newspaper) {
        dataSource.addItem(newspaper)
    }

    override fun addDisk(disk: Disk) {
        dataSource.addItem(disk)
    }

    override fun updateBook(book: Book) {
        dataSource.updateItem(book)
    }

    override fun updateNewspaper(newspaper: Newspaper) {
        dataSource.updateItem(newspaper)
    }

    override fun updateDisk(disk: Disk) {
        dataSource.updateItem(disk)
    }
}