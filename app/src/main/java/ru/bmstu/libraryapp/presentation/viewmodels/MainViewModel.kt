package ru.bmstu.libraryapp.presentation.viewmodels

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.bmstu.common.result.ApiResult
import ru.bmstu.common.types.LibraryMode
import ru.bmstu.data.filters.LibraryFilter
import ru.bmstu.domain.models.LibraryItemType
import ru.bmstu.domain.repositories.GoogleBooksRepository
import ru.bmstu.domain.usecases.AddBookUseCase
import ru.bmstu.domain.usecases.AddDiskUseCase
import ru.bmstu.domain.usecases.AddNewspaperUseCase
import ru.bmstu.domain.usecases.DeleteItemUseCase
import ru.bmstu.domain.usecases.GetAllBooksUseCase
import ru.bmstu.domain.usecases.GetAllDisksUseCase
import ru.bmstu.domain.usecases.GetAllNewspapersUseCase
import ru.bmstu.libraryapp.presentation.viewmodels.state.MainViewState

class MainViewModel(
    private val getAllBooksUseCase: GetAllBooksUseCase,
    private val getAllNewspapersUseCase: GetAllNewspapersUseCase,
    private val getAllDisksUseCase: GetAllDisksUseCase,
    private val deleteItemUseCase: DeleteItemUseCase,
    private val addBookUseCase: AddBookUseCase,
    private val addNewspaperUseCase: AddNewspaperUseCase,
    private val addDiskUseCase: AddDiskUseCase,
    private val googleBooksRepository: GoogleBooksRepository
) : BaseViewModel() {

    private val _libraryItems = MutableStateFlow<List<LibraryItemType>>(emptyList())
    val items: StateFlow<List<LibraryItemType>> = _libraryItems.asStateFlow()

    private var currentMode = LibraryMode.LOCAL

    init {
        loadAllItems()
    }


    fun setLibraryMode(mode: LibraryMode) {
        if (currentMode == mode) return

        currentMode = mode
        when (mode) {
            LibraryMode.LOCAL -> loadAllItems()
            LibraryMode.GOOGLE_BOOKS -> loadGoogleBooks()
        }
    }

    private fun loadGoogleBooks() {
        launchAndHandle(
            onFetch = {
                _state.value = MainViewState.Loading
                googleBooksRepository.searchBooks("popular", "books").fold(
                    onSuccess = { ApiResult.Success(it) },
                    onFailure = { ApiResult.Error(-1, it.message ?: "Failed to load Google Books") }
                )
            },
            onSuccess = { books ->
                _libraryItems.value = books
                _state.value = MainViewState.Success(books)
            }
        )
    }

    private fun loadAllItems() {
        launchAndHandle(
            onFetch = {
                val booksResult = getAllBooksUseCase()
                val newspapersResult = getAllNewspapersUseCase()
                val disksResult = getAllDisksUseCase()

                val allItems = mutableListOf<LibraryItemType>()
                val errors = mutableListOf<String>()

                booksResult.fold(
                    onSuccess = { allItems.addAll(it) },
                    onFailure = { errors.add("books") }
                )

                newspapersResult.fold(
                    onSuccess = { allItems.addAll(it) },
                    onFailure = { errors.add("newspapers") }
                )

                disksResult.fold(
                    onSuccess = { allItems.addAll(it) },
                    onFailure = { errors.add("disks") }
                )

                if (errors.isNotEmpty()) {
                    ApiResult.Error(code = -1, message = "Errors loading: ${errors.joinToString(", ")}")
                } else {
                    ApiResult.Success(allItems)
                }
            },
            onSuccess = {
                _libraryItems.value = it
                _state.value = MainViewState.Success(it)
            }
        )
    }

    fun deleteItem(itemId: Int): LibraryItemType? {
        val item = _libraryItems.value.find { it.id == itemId } ?: return null

        launchAndHandle(
            onFetch = {
                deleteItemUseCase(itemId).let { ApiResult.Success(Unit) }
            },
            onSuccess = {
                refreshItems()
            }
        )

        return item
    }

    fun restoreItem(item: LibraryItemType) {
        launchAndHandle(
            onFetch = {
                when (item) {
                    is LibraryItemType.Book -> addBookUseCase(item)
                    is LibraryItemType.Newspaper -> addNewspaperUseCase(item)
                    is LibraryItemType.Disk -> addDiskUseCase(item)
                }.let { ApiResult.Success(Unit) }
            },
            onSuccess = {
                refreshItems()
            }
        )
    }

    fun updateFilter(filter: LibraryFilter) {
        refreshItems()
    }

    fun refreshItems() {
        when (currentMode) {
            LibraryMode.LOCAL -> loadAllItems()
            LibraryMode.GOOGLE_BOOKS -> loadGoogleBooks()
        }
    }
}