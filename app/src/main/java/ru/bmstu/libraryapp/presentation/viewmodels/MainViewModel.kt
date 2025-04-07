package ru.bmstu.libraryapp.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ru.bmstu.libraryapp.domain.entities.LibraryItem
import ru.bmstu.libraryapp.domain.repositories.LibraryRepository

class MainViewModel(private val repository: LibraryRepository) : ViewModel() {

    private val _libraryItems = MutableLiveData<List<LibraryItem>>()
    val libraryItems: LiveData<List<LibraryItem>> = _libraryItems

    private val _loading = MutableLiveData<Boolean>()

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadAllItems()
    }

    fun loadAllItems() {
        _loading.value = true
        try {
            val items = mutableListOf<LibraryItem>().apply {
                addAll(repository.getAllBooks())
                addAll(repository.getAllNewspapers())
                addAll(repository.getAllDisks())
            }
            _libraryItems.value = items
            _error.value = null
        } catch (e: Exception) {
            _error.value = e.message
        } finally {
            _loading.value = false
        }
    }

    fun deleteItem(itemId: Int) {
        _loading.value = true
        try {
            val success = repository.deleteItem(itemId)
            if (success) {
                _libraryItems.value = _libraryItems.value?.filter { it.id != itemId }
            } else {
                _error.value = "error deleting item"
            }
        } catch (e: Exception) {
            _error.value = e.message
        } finally {
            _loading.value = false
        }
    }

    companion object {
        fun provideFactory(repository: LibraryRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MainViewModel(repository)
            }
        }
    }
}