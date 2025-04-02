package ru.bmstu.libraryapp.presentation.ui.adapters

sealed class DetailMode {
    data object View : DetailMode()
    data object Create : DetailMode()
}