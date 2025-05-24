package ru.bmstu.data.pagination

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

    var currentPageNumber = 0
    private var totalItems = 0
    private var isLoading = false
    private var isLastPage = false
    private var isFirstPage = true

    private val initialLoadSize: Int
        get() = preferences.pageSize * 3

    private val nextLoadSize: Int
        get() = preferences.pageSize

    suspend fun loadMore(forward: Boolean): Page<T> {
        if (isLoading) {
            return Page(emptyList(), currentPageNumber, isLastPage, currentPageNumber == 0)
        }
        isLoading = true

        try {
            if (!forward && currentPageNumber == 0) {
                isFirstPage = true
                return Page(emptyList(), currentPageNumber, isLastPage, isFirstPage)
            }
            if (forward && isLastPage) {
                return Page(emptyList(), currentPageNumber, isLastPage, isFirstPage)
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
            isFirstPage = newPageNumber == 0
            currentPageNumber = newPageNumber

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
}