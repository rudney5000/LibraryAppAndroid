package ru.bmstu.libraryapp.presentation.viewmodels.state

sealed interface MainViewState {
    object Loading : MainViewState
    data class Error(val message: String) : MainViewState
}