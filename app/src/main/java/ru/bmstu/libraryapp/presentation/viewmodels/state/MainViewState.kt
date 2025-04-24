package ru.bmstu.libraryapp.presentation.viewmodels.state

import ru.bmstu.libraryapp.domain.entities.LibraryItemType

sealed interface MainViewState {
    object Loading : MainViewState
    data class Error(val message: String) : MainViewState
    data class Content(val items: List<LibraryItemType>) : MainViewState
}