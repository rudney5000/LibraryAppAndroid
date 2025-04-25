package ru.bmstu.libraryapp.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.bmstu.libraryapp.domain.entities.DetailMode
import ru.bmstu.libraryapp.domain.entities.LibraryItem
import ru.bmstu.libraryapp.domain.entities.LibraryItemType
import ru.bmstu.libraryapp.domain.repositories.LibraryRepository
import ru.bmstu.libraryapp.presentation.utils.LibraryException
import kotlin.coroutines.cancellation.CancellationException

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
        viewModelScope.launch(Dispatchers.IO) {
            _loading.postValue(true)
            try {
                val result = when (mode) {
                    DetailMode.CREATE -> {
                        createItem(updatedItem)
                    }
                    DetailMode.EDIT -> {
                        updateItem(updatedItem)
                    }
                    else -> {
                        Result.failure(LibraryException.UpdateError("Invalid mode"))
                    }
                }

                if (result.isSuccess) {
                    _item.postValue(updatedItem)
                    _saveSuccess.postValue(true)
                } else {
                    throw result.exceptionOrNull() ?: LibraryException.LoadError("Unknown erro")
                }
            } catch (e: CancellationException) {
            } catch (e: Exception) {
                _error.postValue(e.message)
                _saveSuccess.postValue(false)
            } finally {
                _loading.postValue(false)
            }
        }
    }

    private suspend fun createItem(item: LibraryItem): Result<Unit> {
        return when (item) {
            is LibraryItemType.Book -> repository.addBook(item)
            is LibraryItemType.Newspaper -> repository.addNewspaper(item)
            is LibraryItemType.Disk -> repository.addDisk(item)
            else -> Result.failure(LibraryException.SaveError("Type element not supported"))
        }
    }

    private suspend fun updateItem(item: LibraryItem): Result<Unit> {
        return when (item) {
            is LibraryItemType.Book -> repository.updateBook(item)
            is LibraryItemType.Newspaper -> repository.updateNewspaper(item)
            is LibraryItemType.Disk -> repository.updateDisk(item)
            else -> Result.failure(LibraryException.UpdateError("Type element not supported"))
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
            throw LibraryException.LoadError("Unknown ViewModel class")
        }
    }
}