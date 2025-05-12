package ru.bmstu.libraryapp.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.bmstu.libraryapp.data.pagination.PaginationHelper
import ru.bmstu.libraryapp.data.preferences.LibraryPreferences
import ru.bmstu.libraryapp.domain.entities.LibraryItemType
import ru.bmstu.libraryapp.domain.entities.LibraryMode
import ru.bmstu.libraryapp.domain.repositories.GoogleBooksRepository
import ru.bmstu.libraryapp.domain.repositories.LibraryRepository
import ru.bmstu.libraryapp.presentation.viewmodels.state.MainViewState
import java.util.LinkedList

class MainViewModel(
    private val repository: LibraryRepository,
    private val googleBooksRepository: GoogleBooksRepository,
    private val preferences: LibraryPreferences
) : ViewModel() {

    private val paginationHelper by lazy {
        PaginationHelper(
            dataSource = repository.dataSource,
            preferences = preferences,
            type = LibraryItemType::class,
            itemTypeName = "items"
        )
    }

    private val _mode = MutableStateFlow<LibraryMode>(LibraryMode.Local)
    val mode: StateFlow<LibraryMode> = _mode.asStateFlow()

    private val buffer = LinkedList<LibraryItemType>()
    private val bufferWindowSize get() = preferences.pageSize * 3

    private val _libraryItems = MutableStateFlow<List<LibraryItemType>>(emptyList())
    val items: StateFlow<List<LibraryItemType>> = _libraryItems.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _loadingMore = MutableStateFlow(false)
    val loadingMore: StateFlow<Boolean> = _loadingMore.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _state = MutableStateFlow<MainViewState>(MainViewState.Loading)
    val state: StateFlow<MainViewState> = _state.asStateFlow()

    private val _sortOrder = MutableStateFlow(preferences.sortOrder)
    val sortOrder: StateFlow<String> = _sortOrder.asStateFlow()

    val currentPageNumber get() = paginationHelper.currentPageNumber

    init {
        loadAllItems()
    }

    fun refreshItems() {
        viewModelScope.launch {
            when (_mode.value) {
                is LibraryMode.Local -> loadAllItems()
                is LibraryMode.GoogleBooks -> {}
            }
        }
    }

    fun setSortOrder(newSortOrder: String) {
        if (_sortOrder.value != newSortOrder) {
            _sortOrder.value = newSortOrder
            preferences.sortOrder = newSortOrder
            loadAllItems()
        }
    }

    fun loadMoreItems(forward: Boolean) {
        if (_loadingMore.value || state.value is MainViewState.Loading) return

        viewModelScope.launch {
            _loadingMore.value = true
            repository.loadMoreItems(forward).fold(
                onSuccess = { newItems ->
                    if (forward) {
                        buffer.addAll(newItems)
                        if (buffer.size > bufferWindowSize) {
                            val toRemove = minOf(newItems.size, buffer.size - bufferWindowSize)
                            buffer.subList(0, toRemove).clear()
                        }
                    } else {
                        buffer.addAll(0, newItems)
                        if (buffer.size > bufferWindowSize) {
                            val toRemove = minOf(newItems.size, buffer.size - bufferWindowSize)
                            buffer.subList(buffer.size - toRemove, buffer.size).clear()
                        }
                    }
                    _libraryItems.value = buffer.toList()
                    _state.value = MainViewState.Content
                },
                onFailure = { e ->
                    _state.value = MainViewState.Error("Load more failed: ${e.message}")
                }
            )
            _loadingMore.value = false
        }
    }

    private fun loadAllItems() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            _state.value = MainViewState.Loading

            repository.getAllItems().fold(
                onSuccess = { items ->
                    buffer.clear()
                    buffer.addAll(items)
                    _libraryItems.value = buffer.toList()
                    _state.value = MainViewState.Content
                },
                onFailure = { e ->
                    _error.value = "Error loading items: ${e.message}"
                    _state.value = MainViewState.Error(e.message ?: "Unknown error")
                }
            )
            _loading.value = false
        }
    }

    fun searchBooks(author: String? = null, title: String? = null) {
        viewModelScope.launch {
            _loading.value = true
            Log.d("MainViewModel", "searchBooks вызван: author=$author, title=$title")

            googleBooksRepository.searchBooks(author, title).fold(
                onSuccess = { books ->
                    Log.d("MainViewModel", "Получено ${books.size} книг")
                    buffer.clear()
                    buffer.addAll(books)
                    _libraryItems.value = buffer.toList()
                    _state.value = MainViewState.Content
                },
                onFailure = { error ->
                    Log.e("MainViewModel", "Ошибка поиска", error)
                    _error.value = "Ошибка поиска: ${error.message}"
                    _state.value = MainViewState.Error(error.message ?: "Неизвестная ошибка")
                }
            )

            _loading.value = false
        }
    }

//    fun searchBooks(author: String?, title: String?) {
//        viewModelScope.launch {
//            _loading.value = true
//            Log.d("MainViewModel", "Début de la recherche - auteur: $author, titre: $title")
//            _state.value = MainViewState.Loading
//            val result = googleBooksRepository.searchBooks(author, title)
//            googleBooksRepository.searchBooks(author, title)
//                .onSuccess { books ->
//                    Log.d("MainViewModel", "Recherche réussie: ${books.size} résultats")
//                    _state.value = MainViewState.GoogleBooksResults(books)
//                }
//                .onFailure { e ->
//                    Log.e("MainViewModel", "Échec de la recherche", e)
//                    _state.value = MainViewState.Error(e.message ?: "Search failed")
//                }
//        }
//    }

    fun deleteItem(itemId: Int): LibraryItemType? {
        val item = _libraryItems.value.find { it.id == itemId }
        if (item != null) {
            viewModelScope.launch {
                repository.deleteItem(itemId)
                    .onSuccess {
                        refreshItems()
                    }
                    .onFailure {
                        _state.value = MainViewState.Error("Delete failed")
                    }
            }
            return item
        }
        return null
    }

    fun restoreItem(item: LibraryItemType) {
        viewModelScope.launch {
            try {
                when (item) {
                    is LibraryItemType.Book -> repository.addBook(item)
                    is LibraryItemType.Newspaper -> repository.addNewspaper(item)
                    is LibraryItemType.Disk -> repository.addDisk(item)
                }
                refreshItems()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    val isInGoogleBooksMode: Boolean
        get() = _mode.value is LibraryMode.GoogleBooks

    fun saveGoogleBook(book: LibraryItemType.Book) {
        viewModelScope.launch {
            repository.addBook(book)
            refreshItems()
        }
    }

    fun switchToLibrary() {
        if (_mode.value is LibraryMode.Local) return
        _mode.value = LibraryMode.Local
        refreshItems()
    }

    fun switchToGoogleBooks() {
        if (_mode.value is LibraryMode.GoogleBooks) return
        _mode.value = LibraryMode.GoogleBooks
        _libraryItems.value = emptyList()
    }

    companion object {
        fun provideFactory(
            repository: LibraryRepository,
            googleBooksRepository: GoogleBooksRepository,
            preferences: LibraryPreferences
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MainViewModel(repository, googleBooksRepository, preferences) as T
                }
            }
        }
    }
}