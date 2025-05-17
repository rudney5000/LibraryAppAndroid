package ru.bmstu.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.bmstu.common.types.DiskType
import ru.bmstu.common.types.Month
import ru.bmstu.domain.models.LibraryItemType
import java.util.Date

@Entity(tableName = "library_items")
data class LibraryItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val isAvailable: Boolean,
    val itemType: String,
    val pages: Int? = null,
    val author: String? = null,
    val issueNumber: Int? = null,
    val month: Month? = null,
    val diskType: DiskType? = null,
    val createdAt: Date = Date()
) {
    fun toDomain(): LibraryItemType = when(itemType) {
        "BOOK" -> LibraryItemType.Book(id, title, isAvailable, pages!!, author!!)
        "NEWSPAPER" -> LibraryItemType.Newspaper(id, title, isAvailable, issueNumber!!, month!!)
        "DISK" -> LibraryItemType.Disk(id, title, isAvailable, diskType!!)
        else -> throw IllegalArgumentException("Unknown item type: $itemType")
    }

    companion object {
        fun fromDomain(item: LibraryItemType): LibraryItemEntity = when(item) {
            is LibraryItemType.Book -> LibraryItemEntity(
                id = item.id,
                title = item.title,
                isAvailable = item.isAvailable,
                itemType = "BOOK",
                pages = item.pages,
                author = item.author
            )
            is LibraryItemType.Newspaper -> LibraryItemEntity(
                id = item.id,
                title = item.title,
                isAvailable = item.isAvailable,
                itemType = "NEWSPAPER",
                issueNumber = item.issueNumber,
                month = item.month
            )
            is LibraryItemType.Disk -> LibraryItemEntity(
                id = item.id,
                title = item.title,
                isAvailable = item.isAvailable,
                itemType = "DISK",
                diskType = item.type
            )
        }
    }
}