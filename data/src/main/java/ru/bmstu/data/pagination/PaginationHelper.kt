package ru.bmstu.data.pagination

import android.util.Log
import kotlinx.coroutines.delay
import ru.bmstu.common.utils.filterByKClass
import ru.bmstu.data.datasources.LocalDataSource
import ru.bmstu.data.preferences.LibraryPreferences
import ru.bmstu.domain.exceptions.LibraryException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.reflect.KClass

class PaginationHelper<T : Any>(
    private val dataSource: LocalDataSource,
    private val preferences: LibraryPreferences,
    private val type: KClass<T>,
    private val itemTypeName: String
) {
    private val TAG = "PaginationHelper"

    var currentPageNumber = 0
    private var totalItems = 0
    private var isLoading = false
    private var isLastPage = false
    private var isFirstPage = true
    private var lastSortOrder = ""

    private val initialLoadSize: Int
        get() = preferences.pageSize * 3

    private val nextLoadSize: Int
        get() = preferences.pageSize

    suspend fun loadInitial(sortBy: String? = null): Page<T> {
        Log.d(TAG, "Loading initial page with sort order: $sortBy")
        if (isLoading) return Page(emptyList(), 0, true, true)
        isLoading = true

        try {

            val sortOrder = sortBy ?: preferences.sortOrder
            if(lastSortOrder != sortOrder) {
                lastSortOrder = sortOrder
                preferences.sortOrder = sortOrder
            }

            currentPageNumber = 0
            totalItems = dataSource.getItemCount()
            isLastPage = false
            isFirstPage = true

            val items = dataSource.getItemsPage(
                page = currentPageNumber,
                pageSize = initialLoadSize,
                sortBy = sortOrder
            ).filterByKClass(type)

            isLastPage = items.size < initialLoadSize ||
                    (currentPageNumber * initialLoadSize + items.size) >= totalItems

            Log.d(TAG, "Initial load: ${items.size} items, total: $totalItems, isLastPage: $isLastPage")

            return Page(items, currentPageNumber, isLastPage, isFirstPage)
        } finally {
            isLoading = false
        }
    }

    suspend fun loadMore(forward: Boolean): Page<T> {
        if (isLoading) {
            Log.d(TAG, "Load more aborted: already loading")
            return Page(emptyList(), currentPageNumber, isLastPage, currentPageNumber == 0)
        }
        Log.d(TAG, "Loading more, direction: ${if (forward) "forward" else "backward"}, current page: $currentPageNumber")
//        if (isLoading) return Page(emptyList(), currentPageNumber, isLastPage, isFirstPage)
        isLoading = true

        try {
            if (!forward && currentPageNumber == 0) {
                Log.d(TAG, "Can't load previous: already at first page")
                isFirstPage = true
                return Page(emptyList(), currentPageNumber, isLastPage, isFirstPage)
            }
            if (forward && isLastPage) {
                Log.d(TAG, "Can't load next: already at last page")
                return Page(emptyList(), currentPageNumber, isLastPage, isFirstPage)
            }

            val newPageNumber = if (forward) currentPageNumber + 1 else currentPageNumber - 1
            val loadSize = if (newPageNumber == 0) initialLoadSize else nextLoadSize

            Log.d(TAG, "Loading page $newPageNumber with size $loadSize")
            val newItems = dataSource.getItemsPage(
                page = newPageNumber,
                pageSize = loadSize,
                sortBy = preferences.sortOrder
            ).filterByKClass(type)

            isLastPage = forward && (newItems.size < loadSize ||
                    (newPageNumber * loadSize + newItems.size) >= totalItems)
            isFirstPage = newPageNumber == 0
            currentPageNumber = newPageNumber

            Log.d(TAG, "Loaded ${newItems.size} items, new page: $currentPageNumber, isLastPage: $isLastPage")
            return Page(newItems, currentPageNumber, isLastPage, isFirstPage)
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
            Result.failure(LibraryException.LoadError())
        }
    }

    fun resetPagination() {
        currentPageNumber = 0
        isLastPage = false
        isFirstPage = true
    }
}