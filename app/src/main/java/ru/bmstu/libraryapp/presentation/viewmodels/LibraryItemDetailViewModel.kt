package ru.bmstu.libraryapp.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.bmstu.libraryapp.domain.entities.Book
import ru.bmstu.libraryapp.domain.entities.Disk
import ru.bmstu.libraryapp.domain.entities.LibraryItem
import ru.bmstu.libraryapp.domain.entities.Newspaper
import ru.bmstu.libraryapp.domain.repositories.LibraryRepository
import ru.bmstu.libraryapp.presentation.views.ativities.LibraryItemDetailActivity

class LibraryItemDetailViewModel(
    private val repository: LibraryRepository,
    initialItem: LibraryItem?,
    private val mode: LibraryItemDetailActivity.Companion.DetailMode
) : ViewModel() {

    private val _item = MutableLiveData<LibraryItem?>()
    val item: LiveData<LibraryItem?> = _item

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        _item.value = initialItem
    }

    fun saveItem(updatedItem: LibraryItem) {
        _loading.value = true
        try {
            when (mode) {
                LibraryItemDetailActivity.Companion.DetailMode.CREATE -> {
                    when (updatedItem) {
                        is Book -> repository.addBook(updatedItem)
                        is Newspaper -> repository.addNewspaper(updatedItem)
                        is Disk -> repository.addDisk(updatedItem)
                        else -> throw IllegalArgumentException("Type d'élément non pris en charge")
                    }
                }
                LibraryItemDetailActivity.Companion.DetailMode.EDIT -> {
                    when (updatedItem) {
                        is Book -> repository.updateBook(updatedItem)
                        is Newspaper -> repository.updateNewspaper(updatedItem)
                        is Disk -> repository.updateDisk(updatedItem)
                        else -> throw IllegalArgumentException("Type d'élément non pris en charge")
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

    fun updateAvailability(available: Boolean) {
        _item.value?.let { currentItem ->
            repository.updateItemAvailability(currentItem, available)
            _item.value = when (currentItem) {
                is Book -> currentItem.copy(isAvailable = available)
                is Newspaper -> currentItem.copy(isAvailable = available)
                is Disk -> currentItem.copy(isAvailable = available)
                else -> currentItem
            }
        }
    }

    class Factory(
        private val repository: LibraryRepository,
        private val initialItem: LibraryItem?,
        private val mode: LibraryItemDetailActivity.Companion.DetailMode
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