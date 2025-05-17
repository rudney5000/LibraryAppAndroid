package ru.bmstu.libraryapp.presentation.viewmodels.state

import ru.bmstu.domain.models.LibraryItemType

sealed interface MainViewState {
    object Loading : MainViewState
    data class Success(val data: List<LibraryItemType>) : MainViewState
    data class Error(val message: String) : MainViewState
}