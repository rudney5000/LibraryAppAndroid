package ru.bmstu.libraryapp.data.pagination

import kotlinx.coroutines.delay
import ru.bmstu.libraryapp.data.datasources.LocalDataSource
import ru.bmstu.libraryapp.data.preferences.LibraryPreferences
import ru.bmstu.libraryapp.domain.exceptions.LibraryException
import ru.bmstu.libraryapp.presentation.utils.filterByKClass
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.KClass

class PaginationHelper<T : Any>(
    private val dataSource: LocalDataSource,
    private val preferences: LibraryPreferences,
    private val type: KClass<T>,
    private val itemTypeName: String
) {
    var offset = 0
        private set
    
    var totalCount = 0
        private set
        
    var isLoading = false
    
    val currentList = mutableListOf<T>()

    private val initialLoadSize: Int
        get() = preferences.pageSize

    private val nextLoadSize: Int
        get() = preferences.pageSize / 2

    suspend fun loadInitial(): List<T> {
        offset = 0
        totalCount = dataSource.getItemCount()

        val items = dataSource.getAllItems(
            sortBy = preferences.sortOrder,
            limit = initialLoadSize,
            offset = 0
        ).filterByKClass(type)

        currentList.clear()
        currentList.addAll(items)
        return items
    }

    suspend fun loadMore(forward: Boolean): List<T> {
        return if (forward) {
            loadForward()
        } else {
            loadBackward()
        }
    }

    private suspend fun loadForward(): List<T> {
        val newOffset = offset + initialLoadSize
        if (newOffset >= totalCount) return emptyList()

        val newItems = dataSource.getAllItems(
            sortBy = preferences.sortOrder,
            limit = nextLoadSize,
            offset = newOffset
        ).filterByKClass(type)

        if (newItems.isEmpty()) return emptyList()

        offset = newOffset

        if (currentList.size + newItems.size > initialLoadSize) {
            val itemsToRemove = min(newItems.size, currentList.size)
            currentList.subList(0, itemsToRemove).clear()
        }

        currentList.addAll(newItems)
        return newItems
    }

    private suspend fun loadBackward(): List<T> {
        val newOffset = max(0, offset - nextLoadSize)
        if (newOffset == offset) return emptyList()

        val previousItems = dataSource.getAllItems(
            sortBy = preferences.sortOrder,
            limit = nextLoadSize,
            offset = newOffset
        ).filterByKClass(type)

        if (previousItems.isEmpty()) return emptyList()

        offset = newOffset

        if (currentList.size + previousItems.size > initialLoadSize) {
            val itemsToRemove = min(previousItems.size, currentList.size)
            currentList.subList(
                currentList.size - itemsToRemove,
                currentList.size
            ).clear()
        }

        currentList.addAll(0, previousItems)
        return previousItems
    }

    suspend fun <R> handlePaginationRequest(
        isInitialLoad: Boolean,
        block: suspend () -> R
    ): Result<R> {
        return try {
            val startTime = System.currentTimeMillis()

            val result = block()

            if (isInitialLoad) {
                val elapsed = System.currentTimeMillis() - startTime
                val remaining = 1000 - elapsed
                if (remaining > 0) {
                    delay(remaining)
                }
            }

            Result.success(result)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(LibraryException.LoadError(itemTypeName))
        }
    }

}