package ru.bmstu.libraryapp.presentation.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.bmstu.common.types.DetailMode
import ru.bmstu.data.repositories.impl.GoogleBooksRepositoryImpl
import ru.bmstu.domain.models.LibraryItemType
import ru.bmstu.domain.repositories.GoogleBooksRepository
import ru.bmstu.domain.repositories.LibraryRepository
import ru.bmstu.domain.usecases.*

open class ViewModelFactory(
    private val getAllBooksUseCase: GetAllBooksUseCase,
    private val getAllNewspapersUseCase: GetAllNewspapersUseCase,
    private val getAllDisksUseCase: GetAllDisksUseCase,
    private val deleteItemUseCase: DeleteItemUseCase,
    private val addBookUseCase: AddBookUseCase,
    private val addNewspaperUseCase: AddNewspaperUseCase,
    private val addDiskUseCase: AddDiskUseCase,
    private val updateBookUseCase: UpdateBookUseCase,
    private val updateNewspaperUseCase: UpdateNewspaperUseCase,
    private val updateDiskUseCase: UpdateDiskUseCase,
    private val searchBooksUseCase: SearchBooksUseCase,
    private val googleBooksRepository: GoogleBooksRepository,
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(
                    getAllBooksUseCase,
                    getAllNewspapersUseCase,
                    getAllDisksUseCase,
                    deleteItemUseCase,
                    addBookUseCase,
                    addNewspaperUseCase,
                    addDiskUseCase,
                    googleBooksRepository
                ) as T
            }
            modelClass.isAssignableFrom(LibraryItemDetailViewModel::class.java) -> {
                LibraryItemDetailViewModel(
                    addBookUseCase,
                    addNewspaperUseCase,
                    addDiskUseCase,
                    updateBookUseCase,
                    updateNewspaperUseCase,
                    updateDiskUseCase,
                    null,
                    DetailMode.VIEW
                ) as T
            }
            modelClass.isAssignableFrom(SearchViewModel::class.java) -> {
                SearchViewModel(searchBooksUseCase) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        fun create(
            repository: LibraryRepository,
            googleBooksRepository: GoogleBooksRepository? = null,
            content: Context
        ): ViewModelFactory {
            val getAllBooksUseCase = GetAllBooksUseCase(repository)
            val getAllNewspapersUseCase = GetAllNewspapersUseCase(repository)
            val getAllDisksUseCase = GetAllDisksUseCase(repository)
            val deleteItemUseCase = DeleteItemUseCase(repository)
            val addBookUseCase = AddBookUseCase(repository)
            val addNewspaperUseCase = AddNewspaperUseCase(repository)
            val addDiskUseCase = AddDiskUseCase(repository)
            val updateBookUseCase = UpdateBookUseCase(repository)
            val updateNewspaperUseCase = UpdateNewspaperUseCase(repository)
            val updateDiskUseCase = UpdateDiskUseCase(repository)

            val googleBooksRepo = googleBooksRepository ?: GoogleBooksRepositoryImpl(ru.bmstu.data.network.NetworkModule.googleBooksService, content)
            val searchBooksUseCase = SearchBooksUseCase(repository, googleBooksRepo)

            return ViewModelFactory(
                getAllBooksUseCase,
                getAllNewspapersUseCase,
                getAllDisksUseCase,
                deleteItemUseCase,
                addBookUseCase,
                addNewspaperUseCase,
                addDiskUseCase,
                updateBookUseCase,
                updateNewspaperUseCase,
                updateDiskUseCase,
                searchBooksUseCase,
                googleBooksRepo,
                content
            )
        }

        fun create(
            repository: LibraryRepository,
            googleBooksRepository: GoogleBooksRepository? = null,
            content: Context,
            initialItem: LibraryItemType?,
            mode: DetailMode
        ): ViewModelFactory {
            val addBookUseCase = AddBookUseCase(repository)
            val addNewspaperUseCase = AddNewspaperUseCase(repository)
            val addDiskUseCase = AddDiskUseCase(repository)
            val updateBookUseCase = UpdateBookUseCase(repository)
            val updateNewspaperUseCase = UpdateNewspaperUseCase(repository)
            val updateDiskUseCase = UpdateDiskUseCase(repository)
            val googleBooksRepo = googleBooksRepository ?: GoogleBooksRepositoryImpl(ru.bmstu.data.network.NetworkModule.googleBooksService, content)

            return object : ViewModelFactory(
                getAllBooksUseCase = GetAllBooksUseCase(repository),
                getAllNewspapersUseCase = GetAllNewspapersUseCase(repository),
                getAllDisksUseCase = GetAllDisksUseCase(repository),
                deleteItemUseCase = DeleteItemUseCase(repository),
                addBookUseCase = addBookUseCase,
                addNewspaperUseCase = addNewspaperUseCase,
                addDiskUseCase = addDiskUseCase,
                updateBookUseCase = updateBookUseCase,
                updateNewspaperUseCase = updateNewspaperUseCase,
                updateDiskUseCase = updateDiskUseCase,
                searchBooksUseCase = SearchBooksUseCase(repository, googleBooksRepo),
                googleBooksRepository = googleBooksRepo,
                context = content
            ) {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(LibraryItemDetailViewModel::class.java)) {
                        return LibraryItemDetailViewModel(
                            addBookUseCase,
                            addNewspaperUseCase,
                            addDiskUseCase,
                            updateBookUseCase,
                            updateNewspaperUseCase,
                            updateDiskUseCase,
                            initialItem,
                            mode
                        ) as T
                    }
                    return super.create(modelClass)
                }
            }
        }
    }
}