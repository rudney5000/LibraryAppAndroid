package ru.bmstu.libraryapp.data.datasources

import ru.bmstu.libraryapp.data.db.LibraryDatabase
import ru.bmstu.libraryapp.data.db.entities.LibraryItemEntity
import ru.bmstu.libraryapp.domain.entities.LibraryItem
import ru.bmstu.libraryapp.domain.entities.LibraryItemType

class RoomDataSource(private val database: LibraryDatabase) : LocalDataSource {
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
        val item = database.libraryItemDao()
            .getItems("title", 1, 0)
            .firstOrNull { it.id == id } ?: return false
        database.libraryItemDao().delete(item)
        return true
    }

    override suspend fun addItem(item: LibraryItem): Boolean {
        return when (item) {
            is LibraryItemType -> {
                val entity = LibraryItemEntity.fromDomain(item)
                database.libraryItemDao().insert(entity)
                true
            }
            else -> false
        }
    }

    override suspend fun updateItem(item: LibraryItem): Boolean {
        return when (item) {
            is LibraryItemType -> {
                val entity = LibraryItemEntity.fromDomain(item)
                database.libraryItemDao().update(entity)
                true
            }
            else -> false
        }
    }

    override suspend fun getItemCount(): Int {
        return database.libraryItemDao().getItemCount()
    }
}