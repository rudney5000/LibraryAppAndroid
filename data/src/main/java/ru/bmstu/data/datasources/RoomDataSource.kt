package ru.bmstu.data.datasources

import ru.bmstu.common.types.LibraryItem
import ru.bmstu.data.db.LibraryDatabase
import ru.bmstu.data.db.entities.LibraryItemEntity
import ru.bmstu.domain.models.LibraryItemType
import kotlin.coroutines.cancellation.CancellationException

class RoomDataSource(private val database: LibraryDatabase) : LocalDataSource {
    override suspend fun getItemsPage(page: Int, pageSize: Int, sortBy: String): List<LibraryItemType> {
        val offset = page * pageSize
        return database.libraryItemDao()
            .getItems(sortBy, pageSize, offset)
            .map { it.toDomain() }
    }

    @Deprecated("Use getItemsPage instead")
    override suspend fun getAllItems(
        sortBy: String,
        limit: Int,
        offset: Int
    ): List<LibraryItemType> {
        return database.libraryItemDao()
            .getItems(sortBy, limit, offset)
            .map { it.toDomain() }
    }

    override suspend fun deleteItem(id: Int): Boolean {
        return try {
            val result = database.libraryItemDao().deleteById(id)
            result > 0
        } catch (e: CancellationException) {
            false
        }
    }

    override suspend fun addItem(item: LibraryItem): Boolean {
        return try {
            when (item) {
                is LibraryItemType -> {
                    val entity = LibraryItemEntity.fromDomain(item)
                    database.libraryItemDao().insert(entity)
                    true
                }
                else -> false
            }
        } catch (e: CancellationException) {
            false
        }
    }

    override suspend fun updateItem(item: LibraryItem): Boolean {
        return try {
            when (item) {
                is LibraryItemType -> {
                    val entity = LibraryItemEntity.fromDomain(item)
                    database.libraryItemDao().update(entity)
                    true
                }
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getItemCount(): Int {
        return database.libraryItemDao().getItemCount()
    }
}