package ru.bmstu.libraryapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.bmstu.libraryapp.domain.entities.LibraryItemType
import ru.bmstu.libraryapp.domain.repositories.LibraryRepository
import ru.bmstu.libraryapp.presentation.viewmodels.state.MainViewState

class MainViewModel(private val repository: LibraryRepository) : ViewModel() {

    private val _libraryItems = MutableStateFlow<List<LibraryItemType>>(emptyList())
    val items: StateFlow<List<LibraryItemType>> = _libraryItems.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var lastDeletedItem: LibraryItemType? = null

    private val _state = MutableStateFlow<MainViewState>(MainViewState.Loading)
    val state: StateFlow<MainViewState> = _state.asStateFlow()

    init {
        loadAllItems()
    }

    fun refreshItems() {
        loadAllItems()
    }

    private fun loadAllItems() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            _state.value = MainViewState.Loading

            try {

                val allItems = mutableListOf<LibraryItemType>()
                val errorMessages = mutableListOf<String>()

                repository.getAllBooks().fold(
                    onSuccess = { books -> allItems.addAll(books) },
                    onFailure = { errorMessages.add("books") }
                )

                repository.getAllNewspapers().fold(
                    onSuccess = { newspapers -> allItems.addAll(newspapers) },
                    onFailure = { errorMessages.add("newspapers") }
                )

                repository.getAllDisks().fold(
                    onSuccess = { disks -> allItems.addAll(disks) },
                    onFailure = { errorMessages.add("disk") }
                )
                _libraryItems.value = allItems

                if (errorMessages.isNotEmpty()) {
                    _error.value = "Error loading ${errorMessages.joinToString(", ")}"
                }
            } catch (e: Exception) {
                _error.value = "Unexpected error: ${e.message}"
                _libraryItems.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }

    fun undoDelete(item: LibraryItemType) {
        viewModelScope.launch {
            lastDeletedItem?.let { item ->
                try {
                    val result = when (item) {
                        is LibraryItemType.Book -> repository.addBook(item)
                        is LibraryItemType.Newspaper -> repository.addNewspaper(item)
                        is LibraryItemType.Disk -> repository.addDisk(item)
                    }
                    if (result.isSuccess) {
                        refreshItems()
                        lastDeletedItem = null
                    } else {
                        _error.value = "Restoration failed"
                    }
                } catch (e: Exception) {
                    _error.value = e.message
                }
            }
        }
    }

    fun deleteItem(itemId: Int) {
        viewModelScope.launch {
            _state.value = MainViewState.Loading
            repository.deleteItem(itemId)
                .onSuccess {
                    refreshItems()
                }
                .onFailure {
                    _state.value = MainViewState.Error("Delete failed")
                }
        }
    }

    companion object {
        fun provideFactory(repository: LibraryRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MainViewModel(repository) as T
                }
            }
        }
    }
}