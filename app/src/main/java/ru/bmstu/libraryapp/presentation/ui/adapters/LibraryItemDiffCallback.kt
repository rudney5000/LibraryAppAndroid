package ru.bmstu.libraryapp.presentation.ui.adapters

import android.os.Bundle
import androidx.recyclerview.widget.DiffUtil
import ru.bmstu.libraryapp.domain.entities.LibraryItem

class LibraryItemDiffCallback: DiffUtil.ItemCallback<LibraryItem>() {

    override fun areItemsTheSame(oldItem: LibraryItem, newItem: LibraryItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: LibraryItem, newItem: LibraryItem): Boolean {
        return oldItem.isAvailable == newItem.isAvailable &&
                oldItem.title == newItem.title
    }

    override fun getChangePayload(oldItem: LibraryItem, newItem: LibraryItem): Any? {
        return when {
            oldItem.isAvailable != newItem.isAvailable -> Bundle().apply {
                putBoolean("availability_changed", true)
            }
            else -> null
        }
    }
}