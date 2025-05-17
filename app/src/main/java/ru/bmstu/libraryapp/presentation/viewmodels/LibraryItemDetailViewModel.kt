package ru.bmstu.libraryapp.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.bmstu.common.result.ApiResult
import ru.bmstu.common.types.DetailMode
import ru.bmstu.common.types.LibraryItem
import ru.bmstu.domain.models.LibraryItemType
import ru.bmstu.domain.usecases.AddBookUseCase
import ru.bmstu.domain.usecases.AddNewspaperUseCase
import ru.bmstu.domain.usecases.AddDiskUseCase
import ru.bmstu.domain.usecases.UpdateBookUseCase
import ru.bmstu.domain.usecases.UpdateNewspaperUseCase
import ru.bmstu.domain.usecases.UpdateDiskUseCase
import javax.inject.Inject

class LibraryItemDetailViewModel @Inject constructor(
    private val addBookUseCase: AddBookUseCase,
    private val addNewspaperUseCase: AddNewspaperUseCase,
    private val addDiskUseCase: AddDiskUseCase,
    private val updateBookUseCase: UpdateBookUseCase,
    private val updateNewspaperUseCase: UpdateNewspaperUseCase,
    private val updateDiskUseCase: UpdateDiskUseCase,
    initialItem: LibraryItem?,
    private var mode: DetailMode
) : BaseViewModel() {

    private val _item = MutableLiveData<LibraryItem?>()
    val item: LiveData<LibraryItem?> = _item

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess


    init {
        _item.value = initialItem
    }

    fun saveItem(updatedItem: LibraryItem) {
        launchAndHandle(
            onFetch = {
                when (mode) {
                    DetailMode.CREATE -> createItem(updatedItem)
                    DetailMode.EDIT -> updateItem(updatedItem)
                    else -> ApiResult.Error(-1, "Invalid mode")
                }
            },
            onSuccess = {
                _item.postValue(updatedItem)
                _saveSuccess.postValue(true)
            }
        )
    }

    fun initialize(initialItem: LibraryItem?, detailMode: DetailMode) {
        _item.value = initialItem
        mode = detailMode
    }

    private suspend fun createItem(item: LibraryItem): ApiResult<Unit> {
        return when (item) {
            is LibraryItemType.Book -> addBookUseCase(item)
            is LibraryItemType.Newspaper -> addNewspaperUseCase(item)
            is LibraryItemType.Disk -> addDiskUseCase(item)
            else -> ApiResult.Error(-1, "Type not supported")
        } as ApiResult<Unit>
    }

    private suspend fun updateItem(item: LibraryItem): ApiResult<Unit> {
        return when (item) {
            is LibraryItemType.Book -> updateBookUseCase(item)
            is LibraryItemType.Newspaper -> updateNewspaperUseCase(item)
            is LibraryItemType.Disk -> updateDiskUseCase(item)
            else -> ApiResult.Error(-1, "Type not supported")
        } as ApiResult<Unit>
    }
}