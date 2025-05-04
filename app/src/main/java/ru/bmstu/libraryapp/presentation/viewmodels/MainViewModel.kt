package ru.bmstu.libraryapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.bmstu.libraryapp.data.preferences.LibraryPreferences
import ru.bmstu.libraryapp.domain.entities.LibraryItemType
import ru.bmstu.libraryapp.domain.repositories.LibraryRepository
import ru.bmstu.libraryapp.presentation.viewmodels.state.MainViewState
import java.util.LinkedList

class MainViewModel(
    private val repository: LibraryRepository,
    preferences: LibraryPreferences
) : ViewModel() {

    private val _displayItems = MutableStateFlow<List<LibraryItemType>>(emptyList())

    private val buffer = LinkedList<LibraryItemType>()
    private val bufferWindowSize = preferences.pageSize * 3

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

    init {
        loadAllItems()
    }

    fun refreshItems() {
        loadAllItems()
    }

    fun loadMoreItems(forward: Boolean) {
        if (state.value is MainViewState.Loading) return

        viewModelScope.launch {
            _state.value = MainViewState.Loading
            repository.loadMoreItems(forward).fold(
                onSuccess = { newItems ->
                    if (forward) {
                        buffer.addAll(newItems)
                        if (buffer.size > bufferWindowSize) {
                            buffer.subList(0, newItems.size).clear()
                        }
                    } else {
                        buffer.addAll(0, newItems)
                        if (buffer.size > bufferWindowSize) {
                            buffer.subList(bufferWindowSize, buffer.size).clear()
                        }
                    }
                    _displayItems.value = buffer.toList()
                    _state.value = MainViewState.Content
                },
                onFailure = { e ->
                    _state.value = MainViewState.Error("Load more failed: ${e.message}")
                }
            )
        }
    }

    private fun loadAllItems() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            repository.getAllItems().fold(
                onSuccess = { items ->
                    _libraryItems.value = items
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

    companion object {
        fun provideFactory(
            repository: LibraryRepository,
            preferences: LibraryPreferences
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MainViewModel(repository, preferences) as T
                }
            }
        }
    }
}