package ru.bmstu.libraryapp.presentation.viewmodels.state

sealed interface MainViewState {
    object Loading : MainViewState
    object Content : MainViewState
    data class Error(val message: String) : MainViewState
    object LoadingMore : MainViewState
}