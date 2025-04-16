package ru.bmstu.libraryapp.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.bmstu.libraryapp.domain.entities.DetailMode
import ru.bmstu.libraryapp.domain.entities.LibraryItem
import ru.bmstu.libraryapp.domain.entities.LibraryItemType
import ru.bmstu.libraryapp.domain.repositories.LibraryRepository

class LibraryItemDetailViewModel(
    private val repository: LibraryRepository,
    initialItem: LibraryItem?,
    private val mode: DetailMode
) : ViewModel() {

    private val _item = MutableLiveData<LibraryItem?>()
    val item: LiveData<LibraryItem?> = _item

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    private val _loading = MutableLiveData<Boolean>()

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        _item.value = initialItem
    }

    fun saveItem(updatedItem: LibraryItem) {
        _loading.value = true
        try {
            when (mode) {
                DetailMode.CREATE -> {
                    when (updatedItem) {
                        is LibraryItemType.Book -> repository.addBook(updatedItem)
                        is LibraryItemType.Newspaper -> repository.addNewspaper(updatedItem)
                        is LibraryItemType.Disk -> repository.addDisk(updatedItem)
                        else -> throw IllegalArgumentException("Type element not supported")
                    }
                }
                DetailMode.EDIT -> {
                    when (updatedItem) {
                        is LibraryItemType.Book -> repository.updateBook(updatedItem)
                        is LibraryItemType.Newspaper -> repository.updateNewspaper(updatedItem)
                        is LibraryItemType.Disk -> repository.updateDisk(updatedItem)
                        else -> throw IllegalArgumentException("Type element not supported")
                    }
                }
                else -> {}
            }
            _item.value = updatedItem
            _saveSuccess.value = true
        } catch (e: Exception) {
            _error.value = e.message
            _saveSuccess.value = false
        } finally {
            _loading.value = false
        }
    }

    class Factory(
        private val repository: LibraryRepository,
        private val initialItem: LibraryItem?,
        private val mode: DetailMode
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LibraryItemDetailViewModel::class.java)) {
                return LibraryItemDetailViewModel(repository, initialItem, mode) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}