package ru.bmstu.libraryapp.presentation.viewmodels

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.bmstu.common.result.ApiResult
import ru.bmstu.domain.models.LibraryItemType
import ru.bmstu.domain.usecases.SearchBooksUseCase
import ru.bmstu.libraryapp.presentation.viewmodels.state.MainViewState
import ru.bmstu.common.types.LibraryMode

class SearchViewModel(
    private val searchBooksUseCase: SearchBooksUseCase
) : BaseViewModel() {

    private val _searchResults = MutableStateFlow<List<LibraryItemType.Book>>(emptyList())
    val searchResults: StateFlow<List<LibraryItemType.Book>> = _searchResults.asStateFlow()

    private var libraryMode = LibraryMode.LOCAL

    fun setLibraryMode(mode: LibraryMode) {
        libraryMode = mode
    }

    fun searchBooks(query: String) {
        launchAndHandle(
            onFetch = {
                _state.value = MainViewState.Loading
                searchBooksUseCase(query, libraryMode).let { result ->
                    result.fold(
                        onSuccess = { ApiResult.Success(it) },
                        onFailure = { ApiResult.Error(-1, it.message ?: "Search failed") }
                    )
                }
            },
            onSuccess = {
                _searchResults.value = it
                _state.value = MainViewState.Success(it)
            }
        )
    }
}