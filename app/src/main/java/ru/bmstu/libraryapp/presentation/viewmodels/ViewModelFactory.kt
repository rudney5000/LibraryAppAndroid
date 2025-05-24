package ru.bmstu.libraryapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject
import javax.inject.Provider

class ViewModelFactory @Inject constructor(
    private val viewModels: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val provider = viewModels[modelClass] ?: viewModels.entries.firstOrNull {
            modelClass.isAssignableFrom(it.key)
        }?.value

        return provider?.get() as T ?: throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}