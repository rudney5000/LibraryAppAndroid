package ru.bmstu.libraryapp.data.pagination

import kotlinx.coroutines.delay
import ru.bmstu.libraryapp.data.datasources.LocalDataSource
import ru.bmstu.libraryapp.data.preferences.LibraryPreferences
import ru.bmstu.libraryapp.domain.exceptions.LibraryException
import ru.bmstu.libraryapp.presentation.utils.filterByKClass
import kotlin.coroutines.cancellation.CancellationException
import kotlin.reflect.KClass

class PaginationHelper<T : Any>(
    private val dataSource: LocalDataSource,
    private val preferences: LibraryPreferences,
    private val type: KClass<T>,
    private val itemTypeName: String
) {
    var currentPageNumber = 0
    private var totalItems = 0
    private var isLoading = false
    private var isLastPage = false

    private val initialLoadSize: Int
        get() = preferences.pageSize * 3

    private val nextLoadSize: Int
        get() = preferences.pageSize

    suspend fun loadInitial(): Page<T> {
        if (isLoading) return Page(emptyList(), 0, true)
        isLoading = true

        try {
            currentPageNumber = 0
            totalItems = dataSource.getItemCount()
            isLastPage = false

            val items = dataSource.getItemsPage(
                page = currentPageNumber,
                pageSize = initialLoadSize,
                sortBy = preferences.sortOrder
            ).filterByKClass(type)

            isLastPage = items.size < initialLoadSize ||
                    (currentPageNumber * initialLoadSize + items.size) >= totalItems

            return Page(items, currentPageNumber, isLastPage)
        } finally {
            isLoading = false
        }
    }

    suspend fun loadMore(forward: Boolean): Page<T> {
        if (isLoading) return Page(emptyList(), currentPageNumber, isLastPage)
        isLoading = true

        try {
            if (!forward && currentPageNumber == 0) {
                return Page(emptyList(), currentPageNumber, isLastPage)
            }
            if (forward && isLastPage) {
                return Page(emptyList(), currentPageNumber, isLastPage)
            }

            val newPageNumber = if (forward) currentPageNumber + 1 else currentPageNumber - 1
            val loadSize = if (newPageNumber == 0) initialLoadSize else nextLoadSize

            val newItems = dataSource.getItemsPage(
                page = newPageNumber,
                pageSize = loadSize,
                sortBy = preferences.sortOrder
            ).filterByKClass(type)

            isLastPage = forward && (newItems.size < loadSize ||
                    (newPageNumber * loadSize + newItems.size) >= totalItems)
            currentPageNumber = newPageNumber

            return Page(newItems, currentPageNumber, isLastPage)
        } finally {
            isLoading = false
        }
    }

    suspend fun <R> handlePaginationRequest(
        isInitialLoad: Boolean,
        block: suspend () -> R
    ): Result<R> {
        return try {
            val result = block()
            if (isInitialLoad) {
                delay(1000)
            }
            Result.success(result)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(LibraryException.LoadError(itemTypeName))
        }
    }
}