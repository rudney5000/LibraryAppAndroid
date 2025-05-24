package ru.bmstu.libraryapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.bmstu.common.result.ApiResult
import ru.bmstu.libraryapp.presentation.viewmodels.state.MainViewState

abstract class BaseViewModel : ViewModel() {

    protected val _state = MutableStateFlow<MainViewState>(MainViewState.Loading)
    val state: StateFlow<MainViewState> = _state

    protected fun <T> launchAndHandle(
        onFetch: suspend () -> ApiResult<T>,
        onSuccess: (T) -> Unit
    ) {
        viewModelScope.launch {
            _state.value = MainViewState.Loading

            when (val result = onFetch()) {
                is ApiResult.Success -> {
                    onSuccess(result.data)
                }
                is ApiResult.Error -> {
                    _state.value = MainViewState.Error(result.message ?: "Unknown error")
                }
            }
        }
    }
}