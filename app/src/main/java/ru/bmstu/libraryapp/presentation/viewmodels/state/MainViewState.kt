package ru.bmstu.libraryapp.presentation.viewmodels.state

import ru.bmstu.libraryapp.domain.entities.LibraryItemType

sealed interface MainViewState {
    object Loading : MainViewState
    object Content : MainViewState
    data class Error(val message: String) : MainViewState
    object GoogleBooksSearch : MainViewState
    data class GoogleBooksResults(val books: List<LibraryItemType.Book>) : MainViewState
}